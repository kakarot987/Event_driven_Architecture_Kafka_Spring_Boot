package com.eventDrivenKafka.paymentservice.service;
import com.eventDrivenKafka.commonevents.*;
import com.eventDrivenKafka.paymentservice.persistence.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
@Service
public class PaymentChoreographyService {
    private final PaymentService payments;
    private final OutboxRepository outbox;
    private final ProcessedEventRepository processed;
    private final ObjectMapper mapper;
    public PaymentChoreographyService(PaymentService payments,OutboxRepository outbox,
                                     ProcessedEventRepository processed,ObjectMapper mapper){
        this.payments=payments;this.outbox=outbox;this.processed=processed;this.mapper=mapper;
    }
    @Transactional
    public void handle(OrderPlacedEvent incoming,int partition,long offset) throws JsonProcessingException {
        if(processed.findByEventId(incoming.getEventId()).isPresent())return;
        if(outbox.findByAggregateIdAndEventType(incoming.getOrderId(),"PaymentProcessedEvent").isEmpty()){
            var payment=payments.processPayment(incoming.getOrderId(),incoming.getUserId(),
                    incoming.getAmount(),incoming.getItems());
            var emitted=PaymentProcessedEvent.builder()
                    .eventId(UUID.nameUUIDFromBytes(("payment:"+incoming.getOrderId()).getBytes(StandardCharsets.UTF_8)).toString())
                    .orderId(incoming.getOrderId()).paymentId(payment.getPaymentId())
                    .userId(incoming.getUserId()).status(payment.getStatus().name())
                    .transactionId(payment.getTransactionId()).failureReason(payment.getFailureReason())
                    .amount(incoming.getAmount()).items(incoming.getItems())
                    .eventType("PaymentProcessedEvent").timestamp(LocalDateTime.now()).build();
            outbox.save(OutboxEvent.builder().aggregateId(incoming.getOrderId())
                    .eventType("PaymentProcessedEvent").topicName("payment-events")
                    .payload(mapper.writeValueAsString(emitted)).build());
        }
        processed.save(ProcessedEvent.builder().eventId(incoming.getEventId()).aggregateId(incoming.getOrderId())
                .eventType("OrderPlacedEvent").topicName("order-events")
                .partitionId(partition).offset(offset).build());
    }
}
