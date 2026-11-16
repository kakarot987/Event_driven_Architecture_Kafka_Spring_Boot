package com.eventDrivenKafka.orderservice.event;

import com.eventDrivenKafka.orderservice.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderEventPublisherTest {
    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String,String> kafka = mock(KafkaTemplate.class);
    private final OutboxRepository repository = mock(OutboxRepository.class);

    private OutboxEvent pendingEvent() {
        return OutboxEvent.builder().id(42L).aggregateId("order-42").eventType("OrderPlacedEvent")
                .topicName("order-events").payload("{\"eventId\":\"evt42\"}")
                .publishStatus(OutboxEvent.PublishStatus.PENDING)
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    void onlyMarksPublishedAfterBrokerAcknowledges() {
        OutboxEvent event = pendingEvent();
        when(repository.findReady(eq(OutboxEvent.PublishStatus.PENDING), any(), any(Pageable.class)))
                .thenReturn(List.of(event));
        when(kafka.send(anyString(),anyString(),anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        new OrderEventPublisher(kafka,repository).publishPendingEvents();

        assertEquals(OutboxEvent.PublishStatus.PUBLISHED,event.getPublishStatus());
        assertNotNull(event.getPublishedAt());
        verify(repository).save(event);
    }

    @Test
    void failedBrokerDeliveryRemainsEligibleForRetry() {
        OutboxEvent event = pendingEvent();
        when(repository.findReady(eq(OutboxEvent.PublishStatus.PENDING), any(), any(Pageable.class)))
                .thenReturn(List.of(event));
        when(kafka.send(anyString(),anyString(),anyString()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("broker offline")));

        new OrderEventPublisher(kafka,repository).publishPendingEvents();

        assertEquals(OutboxEvent.PublishStatus.PENDING,event.getPublishStatus());
        assertEquals(1,event.getAttempts());
        assertNotNull(event.getNextAttemptAt());
        verify(repository).save(event);
    }
}
