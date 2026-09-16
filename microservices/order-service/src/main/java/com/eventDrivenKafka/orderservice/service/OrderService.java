package com.eventDrivenKafka.orderservice.service;

import com.eventDrivenKafka.orderservice.dto.CreateOrderRequest;
import com.eventDrivenKafka.orderservice.dto.OrderResponse;
import com.eventDrivenKafka.orderservice.persistence.Order;
import com.eventDrivenKafka.orderservice.persistence.Order.OrderStatus;
import com.eventDrivenKafka.orderservice.persistence.OrderRepository;
import com.eventDrivenKafka.orderservice.persistence.OutboxRepository;
import com.eventDrivenKafka.orderservice.persistence.OutboxEvent;
import com.eventDrivenKafka.orderservice.persistence.OutboxEvent.PublishStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository orderRepository, OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        try {
            // Generate unique orderId
            String orderId = UUID.randomUUID().toString();

            // Create Order entity
            Order order = Order.builder()
                    .orderId(orderId)
                    .userId(request.getUserId())
                    .totalAmount(request.getTotalAmount())
                    .status(OrderStatus.PENDING)
                    .orderItems(objectMapper.writeValueAsString(request.getItems()))
                    .build();

            // Save order to DB
            Order savedOrder = orderRepository.save(order);
            log.info("Order created: {}", orderId);

            // Create OutboxEvent for publishing
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(orderId)
                    .eventType("OrderPlacedEvent")
                    .topicName("order-events")
                    .payload(objectMapper.writeValueAsString(createOrderPlacedEventPayload(savedOrder, request)))
                    .publishStatus(PublishStatus.PENDING)
                    .build();

            // Save outbox event (dual-write pattern)
            outboxRepository.save(outboxEvent);
            log.info("OutboxEvent created for order: {}", orderId);

            return mapToResponse(savedOrder);
        } catch (Exception e) {
            log.error("Error creating order", e);
            throw new RuntimeException("Failed to create order", e);
        }
    }

    public OrderResponse getOrder(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    @Transactional
    public void updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus(status);
        orderRepository.save(order);
        log.info("Order status updated: {} -> {}", orderId, status);
    }

    private Object createOrderPlacedEventPayload(Order order, CreateOrderRequest request) {
        return new Object() {
            public final String orderId = order.getOrderId();
            public final String userId = order.getUserId();
            public final Object amount = order.getTotalAmount();
            public final Object items = request.getItems();
            public final LocalDateTime timestamp = LocalDateTime.now();
            public final String eventType = "OrderPlacedEvent";
            public final String eventId = UUID.randomUUID().toString();
        };
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}