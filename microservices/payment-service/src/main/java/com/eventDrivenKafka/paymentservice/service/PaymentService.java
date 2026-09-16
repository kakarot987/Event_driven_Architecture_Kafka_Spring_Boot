package com.eventDrivenKafka.paymentservice.service;

import com.eventDrivenKafka.commonevents.OrderPlacedEvent;
import com.eventDrivenKafka.paymentservice.persistence.Payment;
import com.eventDrivenKafka.paymentservice.persistence.Payment.PaymentStatus;
import com.eventDrivenKafka.paymentservice.persistence.PaymentRepository;
import com.eventDrivenKafka.paymentservice.dto.PaymentResponse;
import com.eventDrivenKafka.paymentservice.dto.OrderPlacedEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;

@Service
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Process payment and return PaymentResponse including items/amount for publisher
     */
    @Transactional
    public PaymentResponse processPayment(String orderId, String userId, BigDecimal amount, List<OrderPlacedEvent.OrderItem> items) {
        try {
            // Check if payment already exists (idempotency)
            Payment existingPayment = paymentRepository.findByOrderId(orderId).orElse(null);
            if (existingPayment != null) {
                log.warn("Payment already exists for order: {}", orderId);
                // Map existing payment and attach items from the incoming event (items are not persisted with Payment)
                PaymentResponse existingResponse = mapToResponse(existingPayment);
                existingResponse.setItems(items);
                return existingResponse;
            }

            String paymentId = UUID.randomUUID().toString();
            String transactionId = UUID.randomUUID().toString();

            // Mock payment processing (simulating success/failure randomly)
            boolean paymentSuccess = simulatePaymentProcessing();

            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .orderId(orderId)
                    .userId(userId)
                    .amount(amount)
                    .status(paymentSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                    .transactionId(paymentSuccess ? transactionId : null)
                    .failureReason(paymentSuccess ? null : "Simulated payment failure - Insufficient funds")
                    .build();

            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment processed for order: {} - Status: {}", orderId, paymentSuccess ? "SUCCESS" : "FAILED");

            PaymentResponse response = mapToResponse(savedPayment);
            response.setItems(items); // attach items to response for event publishing
            return response;
        } catch (Exception e) {
            log.error("Error processing payment for order: {}", orderId, e);
            throw new RuntimeException("Failed to process payment", e);
        }
    }

    public PaymentResponse getPayment(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    /**
     * Simulates payment processing with 80% success rate
     */
    private boolean simulatePaymentProcessing() {
        return Math.random() < 0.8; // 80% success rate
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}