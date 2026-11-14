package com.eventDrivenKafka.inventoryservice.service;

import com.eventDrivenKafka.commonevents.InventoryReservedEvent;
import com.eventDrivenKafka.commonevents.OrderPlacedEvent;
import com.eventDrivenKafka.inventoryservice.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryReservationRepository reservationRepository;
    private final ProcessedEventRepository processedEventRepository;

    public InventoryService(ProductRepository productRepository,
                            InventoryReservationRepository reservationRepository,
                            ProcessedEventRepository processedEventRepository) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
        this.processedEventRepository = processedEventRepository;
    }

    public boolean isEventAlreadyProcessed(String eventId) {
        return processedEventRepository.findByEventId(eventId).isPresent();
    }

    @Transactional
    public InventoryReservedEvent reserveInventory(String eventId, String orderId,
                                                      List<OrderPlacedEvent.OrderItem> requestedItems,
                                                      int partition, long offset) {
        // Idempotency guard
        if (isEventAlreadyProcessed(eventId)) {
            log.warn("Event already processed: {}", eventId);
            return InventoryReservedEvent.builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .orderId(orderId)
                    .status("FAILED")
                    .failureReason("AlreadyProcessed")
                    .timestamp(java.time.LocalDateTime.now())
                    .eventType("InventoryReservedEvent")
                    .build();
        }

        // Check availability
        List<String> insufficient = new ArrayList<>();
        Map<String, Integer> reduced = new HashMap<>();

        for (var item : requestedItems) {
            var optProduct = productRepository.findByProductId(item.getProductId());
            if (optProduct.isEmpty()) {
                insufficient.add(item.getProductId());
                continue;
            }
            var product = optProduct.get();
            if (product.getAvailableQuantity() < item.getQuantity()) {
                insufficient.add(item.getProductId());
            } else {
                reduced.put(product.getProductId(), item.getQuantity());
            }
        }

        if (!insufficient.isEmpty()) {
            String reason = "Insufficient stock for: " + String.join(",", insufficient);
            log.info("Reservation failed for order {}: {}", orderId, reason);
            markEventProcessed(eventId, orderId, "PaymentProcessedEvent", "payment-events", partition, offset);
            return InventoryReservedEvent.builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .orderId(orderId)
                    .status("FAILED")
                    .failureReason(reason)
                    .timestamp(java.time.LocalDateTime.now())
                    .eventType("InventoryReservedEvent")
                    .build();
        }

        // reserve by decrementing stock and saving reservation rows
        List<InventoryReservation> reservations = new ArrayList<>();
        for (var e : reduced.entrySet()) {
            var product = productRepository.findByProductId(e.getKey()).get();
            product.setAvailableQuantity(product.getAvailableQuantity() - e.getValue());
            productRepository.save(product);

            InventoryReservation reservation = InventoryReservation.builder()
                    .reservationId(UUID.randomUUID().toString())
                    .orderId(orderId)
                    .productId(e.getKey())
                    .quantity(e.getValue())
                    .reservedAt(java.time.LocalDateTime.now())
                    .build();
            reservations.add(reservationRepository.save(reservation));
        }

        // mark event processed
        markEventProcessed(eventId, orderId, "PaymentProcessedEvent", "payment-events", partition, offset);

        // build success event DTO
        var items = reservations.stream()
                .map(r -> new OrderPlacedEvent.OrderItem(r.getProductId(), r.getQuantity(), null))
                .collect(Collectors.toList());

        log.info("Inventory reserved for order {}: items={}", orderId, items.size());
        return InventoryReservedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .orderId(orderId)
                .status("RESERVED")
                .items(items)
                .timestamp(java.time.LocalDateTime.now())
                .eventType("InventoryReservedEvent")
                .build();
    }

    private void markEventProcessed(String eventId, String aggregateId, String eventType,
                                    String topicName, int partition, long offset) {
        if (processedEventRepository.findByEventId(eventId).isPresent()) return;
        ProcessedEvent pe = ProcessedEvent.builder()
                .eventId(eventId)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .topicName(topicName)
                .partitionId(partition)
                .offset(offset)
                .build();
        processedEventRepository.save(pe);
    }
}