package com.eventDrivenKafka.shippingservice.service;

import com.eventDrivenKafka.commonevents.*;
import com.eventDrivenKafka.shippingservice.persistence.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShippingChoreographyService {
    private final ShippingService shipping;
    private final OutboxRepository outbox;
    private final ProcessedEventRepository processed;
    private final ObjectMapper mapper;

    public ShippingChoreographyService(ShippingService shipping,OutboxRepository outbox,
            ProcessedEventRepository processed,ObjectMapper mapper){
        this.shipping=shipping;this.outbox=outbox;this.processed=processed;this.mapper=mapper;
    }

    @Transactional
    public void handle(InventoryReservedEvent incoming,int partition,long offset) throws JsonProcessingException {
        if(processed.findByEventId(incoming.getEventId()).isPresent())return;
        if(outbox.findByAggregateIdAndEventType(incoming.getOrderId(),"ShippingCreatedEvent").isEmpty()){
            ShippingCreatedEvent event=shipping.createShipment(incoming);
            event.setEventId(UUID.nameUUIDFromBytes(
                    ("shipping:"+incoming.getOrderId()).getBytes(StandardCharsets.UTF_8)).toString());
            event.setTimestamp(LocalDateTime.now());
            outbox.save(OutboxEvent.builder().aggregateId(incoming.getOrderId())
                    .eventType("ShippingCreatedEvent").topicName("shipping-events")
                    .payload(mapper.writeValueAsString(event)).build());
        }
        processed.save(ProcessedEvent.builder().eventId(incoming.getEventId())
                .aggregateId(incoming.getOrderId()).eventType("InventoryReservedEvent")
                .topicName("inventory-events").partitionId(partition).offset(offset).build());
    }
}
