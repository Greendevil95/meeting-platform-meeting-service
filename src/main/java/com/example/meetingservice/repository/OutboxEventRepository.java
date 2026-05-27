package com.example.meetingservice.repository;

import com.example.meetingservice.entity.OutboxEventEntity;
import com.example.meetingservice.entity.OutboxStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    List<OutboxEventEntity> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    List<OutboxEventEntity> findTop100ByStatusAndProcessingStartedAtBeforeOrderByCreatedAtAsc(
            OutboxStatus status,
            OffsetDateTime processingStartedAt
    );

    Optional<OutboxEventEntity> findByIdAndStatusAndProcessingToken(
            UUID id,
            OutboxStatus status,
            UUID processingToken
    );
}
