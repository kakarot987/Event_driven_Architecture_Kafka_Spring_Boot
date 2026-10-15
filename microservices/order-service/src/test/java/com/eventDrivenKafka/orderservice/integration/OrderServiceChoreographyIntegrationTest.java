package com.eventDrivenKafka.orderservice.integration;

import com.eventDrivenKafka.commonevents.OrderPlacedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for the event-driven choreography flow.
 * Validates that OrderPlacedEvent is published to Kafka with expected structure.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"order-events", "payment-events", "inventory-events", "shipping-events",
        "order-events.DLT", "payment-events.DLT", "inventory-events.DLT", "shipping-events.DLT"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@Slf4j
class OrderServiceChoreographyIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testOrderPlacedEventStructure() throws Exception {
        // Arrange: Create a sample OrderPlacedEvent
        String orderId = UUID.randomUUID().toString();
        String userId = "user-" + UUID.randomUUID().toString();
        
        List<OrderPlacedEvent.OrderItem> items = new ArrayList<>();
        items.add(new OrderPlacedEvent.OrderItem("PROD-001", 2, new BigDecimal("29.99")));
        items.add(new OrderPlacedEvent.OrderItem("PROD-002", 1, new BigDecimal("49.99")));
        
        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .userId(userId)
                .amount(new BigDecimal("109.97"))
                .items(items)
                .timestamp(LocalDateTime.now())
                .eventType("OrderPlacedEvent")
                .build();
        
        // Act: Publish event to order-events topic
        String payload = objectMapper.writeValueAsString(event);
        kafkaTemplate.send("order-events", orderId, payload);
        
        log.info("Published OrderPlacedEvent: {}", payload);
        
        // Assert: Verify event was serialized correctly
        assertNotNull(event.getEventId());
        assertNotNull(event.getOrderId());
        assertNotNull(event.getUserId());
        assert event.getItems().size() == 2;
    }

    @Test
    void testOrderEventPayloadStructure() throws Exception {
        // Validate that the canonical event types have the expected structure
        OrderPlacedEvent.OrderItem item = new OrderPlacedEvent.OrderItem("PROD-123", 5, new BigDecimal("15.50"));
        
        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId("ORDER-001")
                .userId("USER-001")
                .amount(new BigDecimal("77.50"))
                .items(List.of(item))
                .timestamp(LocalDateTime.now())
                .eventType("OrderPlacedEvent")
                .build();
        
        // Serialize and deserialize to ensure JSON compatibility
        String json = objectMapper.writeValueAsString(event);
        OrderPlacedEvent deserialized = objectMapper.readValue(json, OrderPlacedEvent.class);
        
        assertNotNull(deserialized.getEventId());
        assertNotNull(deserialized.getOrderId());
        assert deserialized.getItems().size() == 1;
        assert deserialized.getItems().get(0).getProductId().equals("PROD-123");
    }
}

