package com.eventDrivenKafka.paymentservice.event;
import com.eventDrivenKafka.paymentservice.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
@Component @Slf4j
public class OutboxPublisher {
    private final OutboxRepository outbox;
    private final KafkaTemplate<String,String> kafka;
    public OutboxPublisher(OutboxRepository outbox,KafkaTemplate<String,String> kafka){
        this.outbox=outbox;this.kafka=kafka;
    }
    @Scheduled(fixedDelayString="${app.outbox.poll-ms:2000}")
    @Transactional
    public void publishReady(){
        var ready=outbox.findReady(OutboxEvent.PublishStatus.PENDING,LocalDateTime.now(),PageRequest.of(0,20));
        for(var e:ready){
            try{
                kafka.send(e.getTopicName(),e.getAggregateId(),e.getPayload()).get(10,TimeUnit.SECONDS);
                e.setPublishStatus(OutboxEvent.PublishStatus.PUBLISHED);
                e.setPublishedAt(LocalDateTime.now());
                e.setNextAttemptAt(null);
            }catch(Exception failure){
                int attempt=e.getAttempts()+1;
                e.setAttempts(attempt);
                if(attempt>=8){
                    e.setPublishStatus(OutboxEvent.PublishStatus.FAILED);
                    log.error("Payment outbox {} exhausted attempts; manual replay required",e.getId(),failure);
                }else{
                    e.setNextAttemptAt(LocalDateTime.now().plusSeconds(Math.min(300,1L<<attempt)));
                    log.warn("Payment outbox {} attempt {} failed",e.getId(),attempt);
                }
            }
            outbox.save(e);
        }
    }
}
