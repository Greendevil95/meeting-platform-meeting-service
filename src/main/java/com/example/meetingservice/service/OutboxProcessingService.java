package com.example.meetingservice.service;

import com.example.meetingservice.entity.OutboxEventEntity;
import com.example.meetingservice.entity.OutboxStatus;
import com.example.meetingservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OutboxProcessingService {

    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(5);

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public List<OutboxEventEntity> claimPendingBatch() {
        resetStaleProcessingEvents();

        OffsetDateTime now = OffsetDateTime.now();
        List<OutboxEventEntity> pending = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        pending.forEach(event -> {
            event.setStatus(OutboxStatus.PROCESSING);
            event.setProcessingStartedAt(now);
            event.setProcessingToken(UUID.randomUUID());
        });
        return pending;
    }

    @Transactional
    public void markPublished(UUID eventId, UUID processingToken) {
        outboxEventRepository.findByIdAndStatusAndProcessingToken(eventId, OutboxStatus.PROCESSING, processingToken)
                .ifPresent(event -> {
                    event.setStatus(OutboxStatus.PUBLISHED);
                    event.setProcessingStartedAt(null);
                    event.setProcessingToken(null);
                    event.setPublishedAt(OffsetDateTime.now());
                });
    }

    @Transactional
    public void markFailed(UUID eventId, UUID processingToken) {
        outboxEventRepository.findByIdAndStatusAndProcessingToken(eventId, OutboxStatus.PROCESSING, processingToken)
                .ifPresent(event -> {
                    event.setStatus(OutboxStatus.FAILED);
                    event.setProcessingStartedAt(null);
                    event.setProcessingToken(null);
                    event.setRetryCount(event.getRetryCount() + 1);
                });
    }

    private void resetStaleProcessingEvents() {
        OffsetDateTime staleBefore = OffsetDateTime.now().minus(PROCESSING_TIMEOUT);
        outboxEventRepository.findTop100ByStatusAndProcessingStartedAtBeforeOrderByCreatedAtAsc(
                        OutboxStatus.PROCESSING,
                        staleBefore
                )
                .forEach(event -> {
                    event.setStatus(OutboxStatus.PENDING);
                    event.setProcessingStartedAt(null);
                    event.setProcessingToken(null);
                });
    }
}
