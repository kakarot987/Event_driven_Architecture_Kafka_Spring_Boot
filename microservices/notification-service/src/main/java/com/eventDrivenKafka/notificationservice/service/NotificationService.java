package com.eventDrivenKafka.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendOrderCreated(String userId, String orderId) {
        // Mock send email & sms
        log.info("[NOTIFY] Order created. user={}, order={}", userId, orderId);
    }

    public void sendPaymentUpdate(String userId, String orderId, String status) {
        log.info("[NOTIFY] Payment {} for order {} (user={})", status, orderId, userId);
    }

    public void sendInventoryUpdate(String userId, String orderId, String status) {
        log.info("[NOTIFY] Inventory {} for order {} (user={})", status, orderId, userId);
    }
}