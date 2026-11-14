package com.eventDrivenKafka.orderservice.event;

import com.eventDrivenKafka.orderservice.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component @Slf4j
public class OrderEventPublisher {
    private static final int MAX_ATTEMPTS=8;
    private final KafkaTemplate<String,String> kafka;
    private final OutboxRepository outbox;
    public OrderEventPublisher(KafkaTemplate<String,String> kafka,OutboxRepository outbox){
        this.kafka=kafka;this.outbox=outbox;
    }
    @Scheduled(fixedDelay=5000) @Transactional
    public void publishPendingEvents(){
        var events=outbox.findReady(OutboxEvent.PublishStatus.PENDING,LocalDateTime.now(),PageRequest.of(0,20));
        for(var event:events){
            try{
                kafka.send(event.getTopicName(),event.getAggregateId(),event.getPayload()).get(10,TimeUnit.SECONDS);
                event.setPublishStatus(OutboxEvent.PublishStatus.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
                event.setNextAttemptAt(null);
                log.info("Kafka acknowledged outbox event {}",event.getId());
            }catch(Exception e){
                int tries=event.getAttempts()+1;
                event.setAttempts(tries);
                if(tries>=MAX_ATTEMPTS){
                    event.setPublishStatus(OutboxEvent.PublishStatus.FAILED);
                    log.error("Outbox event {} exhausted retries; inspect FAILED events",event.getId(),e);
                }else{
                    event.setNextAttemptAt(LocalDateTime.now().plusSeconds(Math.min(300,1L<<tries)));
                    log.warn("Outbox event {} will retry (attempt {})",event.getId(),tries);
                }
            }
            outbox.save(event);
        }
        // Ack-then-crash may cause duplicate delivery. Consumers must deduplicate stable event IDs.
    }
}
