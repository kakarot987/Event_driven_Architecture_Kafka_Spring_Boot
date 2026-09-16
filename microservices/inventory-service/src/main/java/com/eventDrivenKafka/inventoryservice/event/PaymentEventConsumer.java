package com.eventDrivenKafka.inventoryservice.event;

import com.eventDrivenKafka.inventoryservice.dto.InventoryReservedEventDto;
import com.eventDrivenKafka.commonevents.PaymentProcessedEvent;
import com.eventDrivenKafka.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Slf4j
public class PaymentEventConsumer {

    private final InventoryService inventoryService;
    private final InventoryEventPublisher inventoryEventPublisher;
    private final ObjectMapper objectMapper;

    public PaymentEventConsumer(InventoryService inventoryService,
                                InventoryEventPublisher inventoryEventPublisher,
                                ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
        this.inventoryEventPublisher = inventoryEventPublisher;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment-events", groupId = "inventory-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentProcessedEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            log.info("Received payment-event key={} partition={} offset={}", key, partition, offset);
            PaymentProcessedEvent event = objectMapper.readValue(message, PaymentProcessedEvent.class);

            if (!"SUCCESS".equalsIgnoreCase(event.getStatus())) {
                log.info("Payment not successful for order {} - skipping inventory reservation", event.getOrderId());
                return;
            }

            // Convert items from payment event into InventoryReservedEventDto.ReservedItem
            var requestedItems = java.util.Optional.ofNullable(event.getItems())
                    .map(list -> {
                        var mapped = new java.util.ArrayList<InventoryReservedEventDto.ReservedItem>();
                        for (var it : list) {
                            mapped.add(new InventoryReservedEventDto.ReservedItem(it.getProductId(), it.getQuantity()));
                        }
                        return mapped;
                    })
                    .orElse(new ArrayList<>());

            // If no items present, fail early
            if (requestedItems.isEmpty()) {
                String reason = "No items found in payment event - cannot reserve";
                log.warn(reason + " for order {}", event.getOrderId());
                inventoryEventPublisher.publishInventoryReservedEvent(InventoryReservedEventDto.failed(event.getOrderId(), reason), event.getOrderId());
                return;
            }

            // Reserve inventory (idempotent)
            InventoryReservedEventDto reservedEvent = inventoryService.reserveInventory(
                    event.getEventId(), event.getOrderId(), requestedItems, partition, offset);

            inventoryEventPublisher.publishInventoryReservedEvent(reservedEvent, event.getOrderId());

        } catch (Exception ex) {
            log.error("Error processing payment event", ex);
            throw new RuntimeException("Failed to process payment event", ex);
        }
    }
}