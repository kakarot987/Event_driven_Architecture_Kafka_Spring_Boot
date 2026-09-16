package com.eventDrivenKafka.notificationservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryReservedEventDto {
    private String eventId;
    private String orderId;
    private String status; // RESERVED | FAILED
    private String failureReason;
    private List<ReservedItem> items;
    private LocalDateTime timestamp;
    private String eventType;
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ReservedItem { private String productId; private Integer quantity; }
}