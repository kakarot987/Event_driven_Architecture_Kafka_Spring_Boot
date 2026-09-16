package com.eventDrivenKafka.paymentservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;
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
    private String status; // SUCCESS or FAILED
    private String transactionId;
    private String failureReason;
    private LocalDateTime timestamp;
    private String eventType;

    // Added context for downstream services
    private BigDecimal amount;
    private List<OrderPlacedEventDto.OrderItem> items;

    public static PaymentProcessedEventDto success(String orderId,
                                                   String paymentId,
                                                   String userId,
                                                   String transactionId,
                                                   BigDecimal amount,
                                                   List<OrderPlacedEventDto.OrderItem> items) {
        return PaymentProcessedEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .paymentId(paymentId)
                .userId(userId)
                .status("SUCCESS")
                .transactionId(transactionId)
                .timestamp(LocalDateTime.now())
                .eventType("PaymentProcessedEvent")
                .amount(amount)
                .items(items)
                .build();
    }

    public static PaymentProcessedEventDto failed(String orderId,
                                                  String userId,
                                                  String failureReason,
                                                  BigDecimal amount,
                                                  List<OrderPlacedEventDto.OrderItem> items) {
        return PaymentProcessedEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .userId(userId)
                .status("FAILED")
                .failureReason(failureReason)
                .timestamp(LocalDateTime.now())
                .eventType("PaymentProcessedEvent")
                .amount(amount)
                .items(items)
                .build();
    }
}