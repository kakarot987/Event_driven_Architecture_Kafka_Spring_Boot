package com.eventDrivenKafka.orderservice.persistence;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="outbox_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxEvent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String aggregateId;
    @Column(nullable=false) private String eventType;
    @Column(columnDefinition="LONGTEXT",nullable=false) private String payload;
    @Column(nullable=false) private String topicName;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private PublishStatus publishStatus;
    @Column(nullable=false) private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    @Builder.Default @Column(nullable=false) private int attempts=0;
    private LocalDateTime nextAttemptAt;
    @PrePersist protected void onCreate(){
        if(createdAt==null) createdAt=LocalDateTime.now();
        if(publishStatus==null) publishStatus=PublishStatus.PENDING;
    }
    public enum PublishStatus {PENDING,PUBLISHED,FAILED}
}
