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
        // A delivery retried after database commit but before Kafka publication must
        // reuse its existing reservation instead of decrementing stock twice.
        var previous = reservationRepository.findByOrderId(orderId);
        if (!previous.isEmpty()) {
            var alreadyReserved = previous.stream()
                    .map(r -> new OrderPlacedEvent.OrderItem(r.getProductId(), r.getQuantity(), null))
                    .collect(Collectors.toList());
            return InventoryReservedEvent.builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .orderId(orderId).status("RESERVED").items(alreadyReserved)
                    .timestamp(java.time.LocalDateTime.now()).eventType("InventoryReservedEvent").build();
        }

        // Check availability
        List<String> insufficient = new ArrayList<>();
        Map<String, Integer> reduced = new LinkedHashMap<>();
        Map<String, Product> locked = new HashMap<>();

        for (var item : requestedItems.stream().sorted(Comparator.comparing(OrderPlacedEvent.OrderItem::getProductId)).toList()) {
            var optProduct = productRepository.findByProductId(item.getProductId());
            if (optProduct.isEmpty()) {
                insufficient.add(item.getProductId());
                continue;
            }
            var product = optProduct.get();
            locked.put(product.getProductId(), product);
            int desired = reduced.getOrDefault(product.getProductId(), 0) + item.getQuantity();
            if (item.getQuantity() == null || item.getQuantity() <= 0 || product.getAvailableQuantity() < desired) {
                insufficient.add(item.getProductId());
            } else {
                reduced.put(product.getProductId(), desired);
            }
        }

        if (!insufficient.isEmpty()) {
            String reason = "Insufficient stock for: " + String.join(",", insufficient);
            log.info("Reservation failed for order {}: {}", orderId, reason);
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
            var product = locked.get(e.getKey());
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

}
