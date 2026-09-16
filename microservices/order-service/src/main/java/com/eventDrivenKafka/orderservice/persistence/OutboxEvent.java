package com.eventDrivenKafka.orderservice.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateId; // orderId

    @Column(nullable = false)
    private String eventType; // e.g., OrderPlacedEvent

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String payload; // JSON

    @Column(nullable = false)
    private String topicName; // order-events

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublishStatus publishStatus; // PENDING, PUBLISHED, FAILED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime publishedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        publishStatus = PublishStatus.PENDING;
    }

    public enum PublishStatus {
        PENDING,
        PUBLISHED,
        FAILED
    }
}