package com.eventDrivenKafka.inventoryservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservedEventDto {
    private String eventId;
    private String orderId;
    private String status; // RESERVED | FAILED
    private String failureReason;
    private List<ReservedItem> items;
    private LocalDateTime timestamp;
    private String eventType;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservedItem {
        private String productId;
        private Integer quantity;
    }

    public static InventoryReservedEventDto success(String orderId, List<ReservedItem> items) {
        return InventoryReservedEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .status("RESERVED")
                .items(items)
                .timestamp(LocalDateTime.now())
                .eventType("InventoryReservedEvent")
                .build();
    }

    public static InventoryReservedEventDto failed(String orderId, String reason) {
        return InventoryReservedEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .status("FAILED")
                .failureReason(reason)
                .timestamp(LocalDateTime.now())
                .eventType("InventoryReservedEvent")
                .build();
    }
}