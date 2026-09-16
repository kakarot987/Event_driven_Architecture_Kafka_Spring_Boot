package com.eventDrivenKafka.shippingservice.controller;

import com.eventDrivenKafka.shippingservice.persistence.Shipment;
import com.eventDrivenKafka.shippingservice.persistence.ShipmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final ShipmentRepository shipmentRepository;

    public ShippingController(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() { return ResponseEntity.ok("Shipping service is running"); }

    @GetMapping("/shipments")
    public ResponseEntity<List<Shipment>> allShipments() { return ResponseEntity.ok(shipmentRepository.findAll()); }
}