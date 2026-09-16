package com.eventDrivenKafka.commonevents;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentProcessedEvent {
    private String eventId;
    private String orderId;
    private String paymentId;
    private String userId;
    private String status; // SUCCESS | FAILED
    private String transactionId;
    private String failureReason;
    private LocalDateTime timestamp;
    private String eventType;

    // Include amount & items so downstream services have full context
    private BigDecimal amount;
    private List<OrderPlacedEvent.OrderItem> items;
}