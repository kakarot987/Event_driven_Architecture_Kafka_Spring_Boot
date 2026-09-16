package com.eventDrivenKafka.inventoryservice.event;

import com.eventDrivenKafka.commonevents.InventoryReservedEvent;
import com.eventDrivenKafka.commonevents.OrderPlacedEvent;
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

            // Items are already modeled as OrderPlacedEvent.OrderItem in the canonical event
            var requestedItems = java.util.Optional.ofNullable(event.getItems()).orElse(new java.util.ArrayList<OrderPlacedEvent.OrderItem>());

            // If no items present, fail early
            if (requestedItems.isEmpty()) {
                String reason = "No items found in payment event - cannot reserve";
                log.warn(reason + " for order {}", event.getOrderId());
                inventoryEventPublisher.publishInventoryReservedEvent(
                        com.eventDrivenKafka.commonevents.InventoryReservedEvent.builder()
                                .eventId(java.util.UUID.randomUUID().toString())
                                .orderId(event.getOrderId())
                                .status("FAILED")
                                .failureReason(reason)
                                .timestamp(java.time.LocalDateTime.now())
                                .eventType("InventoryReservedEvent")
                                .build(), event.getOrderId());
                return;
            }

            // Reserve inventory (idempotent)
            InventoryReservedEvent reservedEvent = inventoryService.reserveInventory(
                    event.getEventId(), event.getOrderId(), requestedItems, partition, offset);

            inventoryEventPublisher.publishInventoryReservedEvent(reservedEvent, event.getOrderId());

        } catch (Exception ex) {
            log.error("Error processing payment event", ex);
            throw new RuntimeException("Failed to process payment event", ex);
        }
    }
}