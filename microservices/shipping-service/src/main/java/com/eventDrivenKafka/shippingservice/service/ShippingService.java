package com.eventDrivenKafka.shippingservice.service;

import com.eventDrivenKafka.commonevents.InventoryReservedEvent;
import com.eventDrivenKafka.commonevents.ShippingCreatedEvent;
import com.eventDrivenKafka.shippingservice.persistence.Shipment;
import com.eventDrivenKafka.shippingservice.persistence.ShipmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class ShippingService {

    private final ShipmentRepository shipmentRepository;

    public ShippingService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public ShippingCreatedEvent createShipment(InventoryReservedEvent inventoryEvent) {
        try {
            String orderId = inventoryEvent.getOrderId();

            // If already shipped, return existing
            var existing = shipmentRepository.findByOrderId(orderId);
            if (existing.isPresent()) {
                var s = existing.get();
                log.warn("Shipment already exists for order {}", orderId);
                return ShippingCreatedEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .orderId(orderId)
                        .shipmentId(s.getShipmentId())
                        .trackingNumber(s.getTrackingNumber())
                        .status("CREATED")
                        .timestamp(LocalDateTime.now())
                        .eventType("SHIPPING_CREATED")
                        .build();
            }

            if (!"RESERVED".equalsIgnoreCase(inventoryEvent.getStatus())) {
                String reason = "Inventory not reserved";
                log.warn("Cannot create shipment for order {}, inventory status {}", orderId, inventoryEvent.getStatus());
                return ShippingCreatedEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .orderId(orderId)
                        .status("FAILED")
                        .timestamp(LocalDateTime.now())
                        .eventType("SHIPPING_FAILED")
                        .build();
            }

            // create shipment
            String shipmentId = UUID.randomUUID().toString();
            String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Shipment shipment = Shipment.builder()
                    .shipmentId(shipmentId)
                    .orderId(orderId)
                    .status("CREATED")
                    .trackingNumber(trackingNumber)
                    .build();

            var saved = shipmentRepository.save(shipment);
            log.info("Created shipment {} for order {}", saved.getShipmentId(), orderId);

            return ShippingCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(orderId)
                    .shipmentId(shipmentId)
                    .trackingNumber(trackingNumber)
                    .status("CREATED")
                    .timestamp(LocalDateTime.now())
                    .eventType("SHIPPING_CREATED")
                    .build();
        } catch (Exception e) {
            log.error("Failed to create shipment", e);
            return ShippingCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(inventoryEvent.getOrderId())
                    .status("FAILED")
                    .timestamp(LocalDateTime.now())
                    .eventType("SHIPPING_FAILED")
                    .build();
        }
    }
}