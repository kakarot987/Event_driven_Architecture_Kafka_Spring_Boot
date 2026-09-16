package com.eventDrivenKafka.inventoryservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProcessedEventDto {
    private String eventId;
    private String orderId;
    private String paymentId;
    private String userId;
    private String status; // SUCCESS | FAILED
    private String transactionId;
    private String failureReason;
    private LocalDateTime timestamp;
    private String eventType;
    private BigDecimal amount;

    // Items from the original order
    private List<OrderItem> items;

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