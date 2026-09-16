package com.eventDrivenKafka.commonevents;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderPlacedEvent {
    private String eventId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private List<OrderItem> items;
    private LocalDateTime timestamp;
    private String eventType;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class OrderItem {
        private String productId;
        private Integer quantity;
        private BigDecimal price;
    }
}