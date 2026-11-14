package com.eventDrivenKafka.inventoryservice.service;

import com.eventDrivenKafka.commonevents.*;
import com.eventDrivenKafka.inventoryservice.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryService {
    private final ProductRepository products;
    private final InventoryReservationRepository reservations;
    private final ProcessedEventRepository processed;

    public InventoryService(ProductRepository products, InventoryReservationRepository reservations,
                            ProcessedEventRepository processed) {
        this.products = products;
        this.reservations = reservations;
        this.processed = processed;
    }

    public boolean isEventAlreadyProcessed(String eventId) {
        return processed.findByEventId(eventId).isPresent();
    }

    @Transactional
    public InventoryReservedEvent reserveInventory(String eventId, String orderId,
                                                    List<OrderPlacedEvent.OrderItem> requestedItems,
                                                    int partition, long offset) {
        // Reconstruct reservations on a Kafka retry after a database commit but before publish ack.
        var previous = reservations.findByOrderId(orderId);
        if (!previous.isEmpty()) {
            return result(orderId, "RESERVED", null, previous.stream()
                    .map(r -> new OrderPlacedEvent.OrderItem(r.getProductId(), r.getQuantity(), null))
                    .collect(Collectors.toList()));
        }

        var totals = new TreeMap<String, Integer>();
        for (var item : requestedItems) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                return result(orderId, "FAILED", "Invalid order quantity", null);
            }
            totals.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        // Lock each product in deterministic order to avoid overselling and reduce deadlocks.
        var locked = new LinkedHashMap<String, Product>();
        var insufficient = new ArrayList<String>();
        for (var entry : totals.entrySet()) {
            var product = products.findByProductId(entry.getKey());
            if (product.isEmpty() || product.get().getAvailableQuantity() < entry.getValue()) {
                insufficient.add(entry.getKey());
            } else {
                locked.put(entry.getKey(), product.get());
            }
        }
        if (!insufficient.isEmpty()) {
            return result(orderId, "FAILED", "Insufficient stock for: " + String.join(",", insufficient), null);
        }

        var reservedItems = new ArrayList<OrderPlacedEvent.OrderItem>();
        for (var entry : totals.entrySet()) {
            Product product = locked.get(entry.getKey());
            product.setAvailableQuantity(product.getAvailableQuantity() - entry.getValue());
            products.save(product);
            reservations.save(InventoryReservation.builder()
                    .reservationId(UUID.randomUUID().toString())
                    .orderId(orderId).productId(entry.getKey()).quantity(entry.getValue())
                    .reservedAt(LocalDateTime.now()).build());
            reservedItems.add(new OrderPlacedEvent.OrderItem(entry.getKey(), entry.getValue(), null));
        }
        // The consumer records the input event only after the downstream Kafka ack.
        return result(orderId, "RESERVED", null, reservedItems);
    }

    private InventoryReservedEvent result(String orderId, String status, String reason,
                                           List<OrderPlacedEvent.OrderItem> items) {
        return InventoryReservedEvent.builder().eventId(UUID.randomUUID().toString())
                .orderId(orderId).status(status).failureReason(reason).items(items)
                .timestamp(LocalDateTime.now()).eventType("InventoryReservedEvent").build();
    }
}
