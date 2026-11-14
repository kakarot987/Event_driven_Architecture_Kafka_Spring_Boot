package com.eventDrivenKafka.orderservice.event;

import com.eventDrivenKafka.commonevents.*;
import com.eventDrivenKafka.orderservice.persistence.Order.OrderStatus;
import com.eventDrivenKafka.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Observes choreography events and updates the source-of-truth order record.
// Distinct consumer group from payment/inventory/shipping business consumers.
@Component @Slf4j
public class OrderStatusEventConsumer {
    private final OrderService orders;
    private final ObjectMapper mapper;

    public OrderStatusEventConsumer(OrderService orders, ObjectMapper mapper) {
        this.orders = orders;
        this.mapper = mapper;
    }

    @KafkaListener(topics="payment-events",groupId="order-status-payment-group")
    public void onPayment(String json) throws Exception {
        var event=mapper.readValue(json,PaymentProcessedEvent.class);
        orders.updateOrderStatus(event.getOrderId(),
                "SUCCESS".equalsIgnoreCase(event.getStatus()) ? OrderStatus.PAYMENT_CONFIRMED : OrderStatus.FAILED);
    }

    @KafkaListener(topics="inventory-events",groupId="order-status-inventory-group")
    public void onInventory(String json) throws Exception {
        var event=mapper.readValue(json,InventoryReservedEvent.class);
        orders.updateOrderStatus(event.getOrderId(),
                "RESERVED".equalsIgnoreCase(event.getStatus()) ? OrderStatus.INVENTORY_RESERVED : OrderStatus.FAILED);
    }

    @KafkaListener(topics="shipping-events",groupId="order-status-shipping-group")
    public void onShipping(String json) throws Exception {
        var event=mapper.readValue(json,ShippingCreatedEvent.class);
        orders.updateOrderStatus(event.getOrderId(),
                "CREATED".equalsIgnoreCase(event.getStatus()) ? OrderStatus.SHIPPED : OrderStatus.FAILED);
    }
}
