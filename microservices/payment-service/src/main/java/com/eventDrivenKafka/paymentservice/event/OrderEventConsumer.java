package com.eventDrivenKafka.paymentservice.event;
import com.eventDrivenKafka.commonevents.OrderPlacedEvent;
import com.eventDrivenKafka.paymentservice.service.PaymentChoreographyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Component;
@Component
public class OrderEventConsumer {
    private final PaymentChoreographyService handler;
    private final ObjectMapper mapper;
    public OrderEventConsumer(PaymentChoreographyService handler,ObjectMapper mapper){
        this.handler=handler;this.mapper=mapper;
    }
    @KafkaListener(topics="order-events",groupId="payment-service-group")
    public void consumeOrderPlacedEvent(@Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) throws Exception {
        handler.handle(mapper.readValue(message,OrderPlacedEvent.class),partition,offset);
    }
}
