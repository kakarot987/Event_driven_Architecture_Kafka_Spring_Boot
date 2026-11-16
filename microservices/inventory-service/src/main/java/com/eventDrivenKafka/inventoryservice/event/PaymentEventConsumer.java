package com.eventDrivenKafka.inventoryservice.event;

import com.eventDrivenKafka.commonevents.PaymentProcessedEvent;
import com.eventDrivenKafka.inventoryservice.service.InventoryChoreographyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {
    private final InventoryChoreographyService handler;
    private final ObjectMapper mapper;
    public PaymentEventConsumer(InventoryChoreographyService handler,ObjectMapper mapper){
        this.handler=handler;this.mapper=mapper;
    }
    @KafkaListener(topics="payment-events",groupId="inventory-service-group")
    public void consumePaymentProcessedEvent(@Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) throws Exception {
        handler.handle(mapper.readValue(message,PaymentProcessedEvent.class),partition,offset);
    }
}
