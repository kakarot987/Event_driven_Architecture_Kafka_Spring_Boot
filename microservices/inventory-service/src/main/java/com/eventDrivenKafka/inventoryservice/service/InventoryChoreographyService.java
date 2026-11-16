package com.eventDrivenKafka.inventoryservice.service;

import com.eventDrivenKafka.commonevents.*;
import com.eventDrivenKafka.inventoryservice.persistence.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryChoreographyService {
    private final InventoryService inventory;
    private final OutboxRepository outbox;
    private final ProcessedEventRepository processed;
    private final ObjectMapper mapper;
    public InventoryChoreographyService(InventoryService inventory,OutboxRepository outbox,
            ProcessedEventRepository processed,ObjectMapper mapper){
        this.inventory=inventory;this.outbox=outbox;this.processed=processed;this.mapper=mapper;
    }

    @Transactional
    public void handle(PaymentProcessedEvent incoming,int partition,long offset) throws JsonProcessingException {
        if(processed.findByEventId(incoming.getEventId()).isPresent()) return;
        if("SUCCESS".equalsIgnoreCase(incoming.getStatus())
                && outbox.findByAggregateIdAndEventType(incoming.getOrderId(),"InventoryReservedEvent").isEmpty()) {
            List<OrderPlacedEvent.OrderItem> items=incoming.getItems();
            InventoryReservedEvent reserved;
            if(items==null || items.isEmpty()){
                reserved=InventoryReservedEvent.builder()
                        .orderId(incoming.getOrderId()).status("FAILED")
                        .failureReason("No order items in payment event").build();
            }else{
                reserved=inventory.reserveInventory(incoming.getEventId(),incoming.getOrderId(),items,partition,offset);
            }
            // A stable event id means at-least-once Kafka delivery is deduplicable.
            reserved.setEventId(UUID.nameUUIDFromBytes(
                    ("inventory:"+incoming.getOrderId()).getBytes(StandardCharsets.UTF_8)).toString());
            reserved.setTimestamp(LocalDateTime.now());
            reserved.setEventType("InventoryReservedEvent");
            outbox.save(OutboxEvent.builder().aggregateId(incoming.getOrderId())
                    .eventType("InventoryReservedEvent").topicName("inventory-events")
                    .payload(mapper.writeValueAsString(reserved)).build());
        }
        processed.save(ProcessedEvent.builder().eventId(incoming.getEventId())
                .aggregateId(incoming.getOrderId()).eventType("PaymentProcessedEvent")
                .topicName("payment-events").partitionId(partition).offset(offset).build());
    }
}
