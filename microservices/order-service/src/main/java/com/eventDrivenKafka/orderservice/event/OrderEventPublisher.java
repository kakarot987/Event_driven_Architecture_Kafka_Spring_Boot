package com.eventDrivenKafka.orderservice.event;

import com.eventDrivenKafka.orderservice.persistence.OutboxEvent;
import com.eventDrivenKafka.orderservice.persistence.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OrderEventPublisher(KafkaTemplate<String, String> kafkaTemplate, OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Polls pending outbox events and publishes them to Kafka
     * Runs every 5 seconds
     */
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                publishEvent(event);
                markEventAsPublished(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event: {}", event.getId(), e);
                markEventAsFailed(event);
            }
        }
    }

    private void publishEvent(OutboxEvent event) throws Exception {
        String topicName = event.getTopicName();
        String aggregateId = event.getAggregateId();
        String payload = event.getPayload();

        // Create Kafka message with orderId as key (for partitioning)
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topicName)
                .setHeader(KafkaHeaders.KEY, aggregateId)
                .build();

        kafkaTemplate.send(message);
        log.info("Event published to topic '{}' with key '{}'", topicName, aggregateId);
    }

    @Transactional
    private void markEventAsPublished(OutboxEvent event) {
        event.setPublishStatus(OutboxEvent.PublishStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        outboxRepository.save(event);
        log.info("Outbox event marked as published: {}", event.getId());
    }

    @Transactional
    private void markEventAsFailed(OutboxEvent event) {
        event.setPublishStatus(OutboxEvent.PublishStatus.FAILED);
        outboxRepository.save(event);
        log.warn("Outbox event marked as failed: {}", event.getId());
    }
}