package com.eventDrivenKafka.notificationservice.event;

import com.eventDrivenKafka.commonevents.OrderPlacedEvent;
import com.eventDrivenKafka.commonevents.PaymentProcessedEvent;
import com.eventDrivenKafka.commonevents.InventoryReservedEvent;
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
    private final ObjectMapper objectMapper;

    public NotificationEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void onOrderEvent(@Payload String message,
                             @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);
            log.info("NotificationService received OrderPlacedEvent orderId={}", event.getOrderId());
            notificationService.sendOrderCreated(event.getUserId(), event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process order event for notification", e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void onPaymentEvent(@Payload String message,
                               @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            PaymentProcessedEvent event = objectMapper.readValue(message, PaymentProcessedEvent.class);
            log.info("NotificationService received PaymentProcessedEvent orderId={} status={}", event.getOrderId(), event.getStatus());
            notificationService.sendPaymentUpdate(event.getUserId(), event.getOrderId(), event.getStatus());
        } catch (Exception e) {
            log.error("Failed to process payment event for notification", e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "inventory-events", groupId = "notification-service-group")
    public void onInventoryEvent(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            InventoryReservedEvent event = objectMapper.readValue(message, InventoryReservedEvent.class);
            log.info("NotificationService received InventoryReservedEvent orderId={} status={}", event.getOrderId(), event.getStatus());
            // No userId in inventory event by default — for a richer flow include userId or resolve via order-view
            notificationService.sendInventoryUpdate("unknown-user", event.getOrderId(), event.getStatus());
        } catch (Exception e) {
            log.error("Failed to process inventory event for notification", e);
            throw new RuntimeException(e);
        }
    }
}