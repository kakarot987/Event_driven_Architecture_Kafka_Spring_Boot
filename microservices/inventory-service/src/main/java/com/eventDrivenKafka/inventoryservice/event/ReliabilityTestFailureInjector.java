package com.eventDrivenKafka.inventoryservice.event;

import com.eventDrivenKafka.commonevents.PaymentProcessedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CI-only deterministic transient failure. This bean is ABSENT unless the explicit
 * reliability-test Spring profile is enabled. Never enable that profile in production.
 * Two Kafka retries plus the initial attempt land a record on its dead-letter topic;
 * replay of that same event on the fourth attempt succeeds.
 */
@Component
@Profile("reliability-test")
public class ReliabilityTestFailureInjector {
    private final ConcurrentHashMap<String, AtomicInteger> attempts = new ConcurrentHashMap<>();

    public void maybeFail(PaymentProcessedEvent event) {
        if (event.getItems() == null || event.getEventId() == null) return;
        boolean marked = event.getItems().stream().anyMatch(
                item -> item.getProductId() != null && item.getProductId().startsWith("DLT-PROBE-"));
        if (!marked) return;
        int count = attempts.computeIfAbsent(event.getEventId(),
                ignored -> new AtomicInteger()).incrementAndGet();
        if (count <= 3) {
            throw new IllegalStateException("CI reliability-test: transient inventory failure " + count);
        }
    }
}
