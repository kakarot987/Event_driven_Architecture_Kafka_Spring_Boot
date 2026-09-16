package com.eventDrivenKafka.paymentservice.dto;

import com.eventDrivenKafka.commonevents.OrderPlacedEvent;
import com.eventDrivenKafka.paymentservice.persistence.Payment.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Items from the original order (not persisted on Payment entity in current model)
    private List<OrderPlacedEvent.OrderItem> items;
}