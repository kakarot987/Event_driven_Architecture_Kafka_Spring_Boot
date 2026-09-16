package com.eventDrivenKafka.paymentservice.event;

import com.eventDrivenKafka.commonevents.OrderPlacedEvent;
import com.eventDrivenKafka.paymentservice.service.IdempotencyService;
import com.eventDrivenKafka.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventConsumer {

    private final PaymentService paymentService;
    private final PaymentEventPublisher paymentEventPublisher;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(PaymentService paymentService, PaymentEventPublisher paymentEventPublisher,
                              IdempotencyService idempotencyService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.paymentEventPublisher = paymentEventPublisher;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    /**
     * Consume OrderPlacedEvent from order-events topic
     * Partitions ensure ordering per orderId (key = orderId)
     */
    @KafkaListener(
            topics = "order-events",
            groupId = "payment-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderPlacedEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partitionId,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            log.info("Received event from partition {} with offset {}: key={}", partitionId, offset, key);

            OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);
            String eventId = event.getEventId();
            String orderId = event.getOrderId();

            // Check if event already processed (idempotency)
            if (idempotencyService.isEventAlreadyProcessed(eventId)) {
                log.warn("Event already processed, skipping: {} (orderId: {})", eventId, orderId);
                return;
            }

            log.info("Processing OrderPlacedEvent for order: {}", orderId);

            // Process payment — pass amount and items so PaymentProcessedEvent carries them forward
            var paymentResponse = paymentService.processPayment(
                    orderId,
                    event.getUserId(),
                    event.getAmount(),
                    event.getItems()
            );
            // Mark event as processed
            idempotencyService.markEventAsProcessed(
                    eventId,
                    orderId,
                    "OrderPlacedEvent",
                    "order-events",
                    partitionId,
                    offset
            );

            // Publish PaymentProcessedEvent (will include items + amount)
            paymentEventPublisher.publishPaymentProcessedEvent(paymentResponse);

            log.info("Payment processing completed for order: {}", orderId);

        } catch (Exception e) {
            log.error("Error processing order event", e);
            // Message will be sent to DLT topic by Spring Kafka error handler
            throw new RuntimeException("Failed to process order event", e);
        }
    }
}