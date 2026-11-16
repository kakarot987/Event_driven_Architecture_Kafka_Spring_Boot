package com.eventDrivenKafka.inventoryservice.persistence;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name="outbox_events", uniqueConstraints=@UniqueConstraint(name="uk_inventory_outbox_aggregate_type",columnNames={"aggregate_id","event_type"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxEvent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="aggregate_id",nullable=false) private String aggregateId;
    @Column(name="event_type",nullable=false) private String eventType;
    @Column(columnDefinition="LONGTEXT",nullable=false) private String payload;
    @Column(nullable=false) private String topicName;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private PublishStatus publishStatus=PublishStatus.PENDING;
    @Builder.Default @Column(nullable=false) private int attempts=0;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime nextAttemptAt;
    @PrePersist void init(){if(createdAt==null)createdAt=LocalDateTime.now();}
    public enum PublishStatus {PENDING,PUBLISHED,FAILED}
}
