package com.eventDrivenKafka.shippingservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShippingCreatedEventDto {
    private String eventId;
    private String orderId;
    private String shipmentId;
    private String trackingNumber;
    private String status; // CREATED | FAILED
    private String eventType;
    private LocalDateTime timestamp;

    public static ShippingCreatedEventDto created(String orderId, String shipmentId, String trackingNumber) {
        return ShippingCreatedEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .shipmentId(shipmentId)
                .trackingNumber(trackingNumber)
                .status("CREATED")
                .eventType("ShippingCreatedEvent")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ShippingCreatedEventDto failed(String orderId, String reason) {
        return ShippingCreatedEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .status("FAILED")
                .eventType("ShippingCreatedEvent")
                .timestamp(LocalDateTime.now())
                .build();
    }
}