package com.eventDrivenKafka.paymentservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlacedEventDto {

    private String orderId;
    private String userId;
    private BigDecimal amount;
    private List<OrderItem> items;
    private LocalDateTime timestamp;
    private String eventType;
    private String eventId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String productId;
        private Integer quantity;
        private BigDecimal price;
    }
}