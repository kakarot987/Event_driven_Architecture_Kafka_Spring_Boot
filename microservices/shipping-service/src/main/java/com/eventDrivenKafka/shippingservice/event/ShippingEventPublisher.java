package com.eventDrivenKafka.shippingservice.event;

import com.eventDrivenKafka.commonevents.ShippingCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShippingEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ShippingEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishShippingCreatedEvent(ShippingCreatedEvent event, String orderId) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            Message<String> message = MessageBuilder.withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, "shipping-events")
                    .setHeader(KafkaHeaders.KEY, orderId)
                    .build();
            kafkaTemplate.send(message);
            log.info("Published shipping event for order {} status={}", orderId, event.getStatus());
        } catch (Exception e) {
            log.error("Failed to publish shipping event for order {}", orderId, e);
            throw new RuntimeException(e);
        }
    }
}