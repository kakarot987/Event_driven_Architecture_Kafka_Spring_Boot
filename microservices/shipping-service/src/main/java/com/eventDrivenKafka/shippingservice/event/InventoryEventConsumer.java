package com.eventDrivenKafka.shippingservice.event;

import com.eventDrivenKafka.commonevents.InventoryReservedEvent;
import com.eventDrivenKafka.shippingservice.service.ShippingChoreographyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventConsumer {
    private final ShippingChoreographyService handler;
    private final ObjectMapper mapper;
    public InventoryEventConsumer(ShippingChoreographyService handler,ObjectMapper mapper){
        this.handler=handler;this.mapper=mapper;
    }
    @KafkaListener(topics="inventory-events",groupId="shipping-service-group")
    public void onInventoryEvent(@Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) throws Exception {
        handler.handle(mapper.readValue(message,InventoryReservedEvent.class),partition,offset);
    }
}
