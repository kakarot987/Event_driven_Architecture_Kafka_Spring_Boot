package com.eventDrivenKafka.inventoryservice.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events", indexes = {
        @Index(name = "idx_event_id", columnList = "eventId"),
        @Index(name = "idx_aggregate_id", columnList = "aggregateId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String aggregateId; // orderId

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String topicName;

    @Column(nullable = false)
    private Integer partitionId;

    @Column(nullable = false)
    private Long offset;

    @Column(nullable = false)
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        processedAt = LocalDateTime.now();
    }
}