package com.eventDrivenKafka.paymentservice.service;

import com.eventDrivenKafka.paymentservice.persistence.ProcessedEvent;
import com.eventDrivenKafka.paymentservice.persistence.ProcessedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class IdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    public IdempotencyService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    /**
     * Check if event has already been processed
     */
    public boolean isEventAlreadyProcessed(String eventId) {
        return processedEventRepository.findByEventId(eventId).isPresent();
    }

    /**
     * Mark event as processed to prevent duplicate processing
     */
    @Transactional
    public void markEventAsProcessed(String eventId, String aggregateId, String eventType,
                                     String topicName, Integer partitionId, Long offset) {
        if (isEventAlreadyProcessed(eventId)) {
            log.warn("Event already processed: {}", eventId);
            return;
        }

        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventId(eventId)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .topicName(topicName)
                .partitionId(partitionId)
                .offset(offset)
                .build();

        processedEventRepository.save(processedEvent);
        log.info("Event marked as processed: {} (offset: {})", eventId, offset);
    }

    /**
     * Get processed event details
     */
    public Optional<ProcessedEvent> getProcessedEvent(String eventId) {
        return processedEventRepository.findByEventId(eventId);
    }
}