package com.eventDrivenKafka.notificationservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}
