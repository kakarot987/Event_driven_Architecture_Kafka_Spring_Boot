package com.eventDrivenKafka.paymentservice.event;

import com.eventDrivenKafka.commonevents.PaymentProcessedEvent;
import com.eventDrivenKafka.paymentservice.dto.PaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish PaymentProcessedEvent to payment-events topic
     * Key = orderId to preserve ordering
     */
    public void publishPaymentProcessedEvent(PaymentResponse paymentResponse) {
        try {
            PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(paymentResponse.getOrderId())
                    .paymentId(paymentResponse.getPaymentId())
                    .userId(paymentResponse.getUserId())
                    .status(paymentResponse.getStatus() != null ? paymentResponse.getStatus().toString() : "UNKNOWN")
                    .transactionId(paymentResponse.getTransactionId())
                    .failureReason(paymentResponse.getFailureReason())
                    .timestamp(LocalDateTime.now())
                    .eventType("PaymentProcessedEvent")
                    .amount(paymentResponse.getAmount())
                    .items(paymentResponse.getItems())
                    .build();

            String payload = objectMapper.writeValueAsString(event);

            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, "payment-events")
                    .setHeader(KafkaHeaders.KEY, paymentResponse.getOrderId())
                    .build();

            kafkaTemplate.send(message);
            log.info("PaymentProcessedEvent published for order: {} - Status: {}",
                    paymentResponse.getOrderId(), paymentResponse.getStatus());

        } catch (Exception e) {
            log.error("Error publishing payment event", e);
            throw new RuntimeException("Failed to publish payment event", e);
        }
    }
}