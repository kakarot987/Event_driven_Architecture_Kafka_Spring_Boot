package com.eventDrivenKafka.commonevents;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryReservedEvent {
    private String eventId;
    private String orderId;
    private String status; // RESERVED | FAILED
    private String failureReason;
    private List<OrderPlacedEvent.OrderItem> items;
    private LocalDateTime timestamp;
    private String eventType;
}