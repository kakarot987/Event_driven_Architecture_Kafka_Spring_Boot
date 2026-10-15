package com.eventDrivenKafka.shippingservice.event;

import com.eventDrivenKafka.commonevents.InventoryReservedEvent;
import com.eventDrivenKafka.commonevents.ShippingCreatedEvent;
import com.eventDrivenKafka.shippingservice.service.IdempotencyService;
import com.eventDrivenKafka.shippingservice.service.ShippingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryEventConsumer {

    private final ShippingService shippingService;
    private final ShippingEventPublisher shippingEventPublisher;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public InventoryEventConsumer(ShippingService shippingService,
                                  ShippingEventPublisher shippingEventPublisher,
                                  IdempotencyService idempotencyService,
                                  ObjectMapper objectMapper) {
        this.shippingService = shippingService;
        this.shippingEventPublisher = shippingEventPublisher;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "inventory-events", groupId = "shipping-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onInventoryEvent(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            InventoryReservedEvent event = objectMapper.readValue(message, InventoryReservedEvent.class);
            log.info("ShippingService received inventory event for order {} status={}", event.getOrderId(), event.getStatus());

            // Check if event already processed (idempotency)
            if (idempotencyService.isEventAlreadyProcessed(event.getEventId())) {
                log.info("Inventory event already processed, skipping: {}", event.getEventId());
                return;
            }

            // Create shipment (idempotent by repository check)
            ShippingCreatedEvent shippingEvent = shippingService.createShipment(event);

            // Mark event as processed
            idempotencyService.markEventAsProcessed(
                    event.getEventId(), event.getOrderId(), "InventoryReservedEvent",
                    "inventory-events", partition, offset);

            // Publish shipping event keyed by orderId
            shippingEventPublisher.publishShippingCreatedEvent(shippingEvent, event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process inventory event in shipping service", e);
            throw new RuntimeException(e);
        }
    }
}