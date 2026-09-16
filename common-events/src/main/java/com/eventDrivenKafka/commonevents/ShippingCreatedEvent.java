package com.eventDrivenKafka.commonevents;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShippingCreatedEvent {
    private String eventId;
    private String orderId;
    private String shipmentId;
    private String trackingNumber;
    private String status; // CREATED | FAILED
    private LocalDateTime timestamp;
    private String eventType;
}