package com.eventDrivenKafka.inventoryservice.persistence;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDateTime;
import java.util.*;
public interface OutboxRepository extends JpaRepository<OutboxEvent,Long> {
    Optional<OutboxEvent> findByAggregateIdAndEventType(String aggregateId,String eventType);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OutboxEvent o where o.publishStatus = :status and (o.nextAttemptAt is null or o.nextAttemptAt <= :now) order by o.createdAt asc")
    List<OutboxEvent> findReady(OutboxEvent.PublishStatus status, LocalDateTime now, Pageable page);
}
