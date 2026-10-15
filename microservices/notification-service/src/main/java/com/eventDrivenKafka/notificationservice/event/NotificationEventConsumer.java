package com.eventDrivenKafka.notificationservice.event;

import com.eventDrivenKafka.commonevents.OrderPlacedEvent;
import com.eventDrivenKafka.commonevents.PaymentProcessedEvent;
import com.eventDrivenKafka.commonevents.InventoryReservedEvent;
import com.eventDrivenKafka.notificationservice.service.IdempotencyService;
import com.eventDrivenKafka.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public NotificationEventConsumer(NotificationService notificationService,
                                     IdempotencyService idempotencyService,
                                     ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onOrderEvent(@Payload String message,
                             @Header(KafkaHeaders.RECEIVED_KEY) String key,
                             @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                             @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);
            
            // Check if event already processed (idempotency)
            if (idempotencyService.isEventAlreadyProcessed(event.getEventId())) {
                log.info("Order event already processed, skipping: {}", event.getEventId());
                return;
            }
            
            log.info("NotificationService received OrderPlacedEvent orderId={}", event.getOrderId());
            notificationService.sendOrderCreated(event.getUserId(), event.getOrderId());
            
            // Mark event as processed
            idempotencyService.markEventAsProcessed(
                    event.getEventId(), event.getOrderId(), "OrderPlacedEvent",
                    "order-events", partition, offset);
        } catch (Exception e) {
            log.error("Failed to process order event for notification", e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onPaymentEvent(@Payload String message,
                               @Header(KafkaHeaders.RECEIVED_KEY) String key,
                               @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                               @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            PaymentProcessedEvent event = objectMapper.readValue(message, PaymentProcessedEvent.class);
            
            // Check if event already processed (idempotency)
            if (idempotencyService.isEventAlreadyProcessed(event.getEventId())) {
                log.info("Payment event already processed, skipping: {}", event.getEventId());
                return;
            }
            
            log.info("NotificationService received PaymentProcessedEvent orderId={} status={}", event.getOrderId(), event.getStatus());
            notificationService.sendPaymentUpdate(event.getUserId(), event.getOrderId(), event.getStatus());
            
            // Mark event as processed
            idempotencyService.markEventAsProcessed(
                    event.getEventId(), event.getOrderId(), "PaymentProcessedEvent",
                    "payment-events", partition, offset);
        } catch (Exception e) {
            log.error("Failed to process payment event for notification", e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "inventory-events", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onInventoryEvent(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            InventoryReservedEvent event = objectMapper.readValue(message, InventoryReservedEvent.class);
            
            // Check if event already processed (idempotency)
            if (idempotencyService.isEventAlreadyProcessed(event.getEventId())) {
                log.info("Inventory event already processed, skipping: {}", event.getEventId());
                return;
            }
            
            log.info("NotificationService received InventoryReservedEvent orderId={} status={}", event.getOrderId(), event.getStatus());
            // No userId in inventory event by default — for a richer flow include userId or resolve via order-view
            notificationService.sendInventoryUpdate("unknown-user", event.getOrderId(), event.getStatus());
            
            // Mark event as processed
            idempotencyService.markEventAsProcessed(
                    event.getEventId(), event.getOrderId(), "InventoryReservedEvent",
                    "inventory-events", partition, offset);
        } catch (Exception e) {
            log.error("Failed to process inventory event for notification", e);
            throw new RuntimeException(e);
        }
    }
}