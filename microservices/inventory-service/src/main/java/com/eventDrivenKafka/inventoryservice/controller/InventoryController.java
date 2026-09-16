package com.eventDrivenKafka.inventoryservice.controller;

import com.eventDrivenKafka.inventoryservice.persistence.Product;
import com.eventDrivenKafka.inventoryservice.persistence.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Slf4j
public class InventoryController {

    private final ProductRepository productRepository;

    public InventoryController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Inventory service is running");
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> allProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    // simple seed endpoint to add product (useful in dev)
    @PostMapping("/products")
    public ResponseEntity<Product> addProduct(@RequestBody Product p) {
        var saved = productRepository.save(p);
        return ResponseEntity.ok(saved);
    }
}