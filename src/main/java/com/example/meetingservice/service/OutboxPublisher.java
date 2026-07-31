package com.example.meetingservice.service;

import com.example.meetingservice.config.KafkaTopicsProperties;
import com.example.meetingservice.entity.OutboxEventEntity;
import com.example.meetingservice.kafka.meeting.EventType;
import com.example.meetingservice.kafka.meeting.MeetingEvent;
import com.example.meetingservice.metrics.MeetingMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Component
public class OutboxPublisher {
    private final OutboxProcessingService outboxProcessingService;
    private final KafkaTopicsProperties topicsProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final MeetingMetrics meetingMetrics;

    @Scheduled(fixedDelayString = "PT3S")
    public void publishPending() {
        List<OutboxEventEntity> pending = outboxProcessingService.claimPendingBatch();
        for (OutboxEventEntity event : pending) {
            try {
                MeetingEvent kafkaEvent = jsonMapper.convertValue(event.getEventJson(), event.getEventType().eventClass());
                String topic = resolveTopic(event.getEventType());
                try (var ignored = OutboxTraceContext.makeCurrent(event.getTraceparent())) {
                    kafkaTemplate.send(topic, event.getAggregateId(), kafkaEvent).get();
                }
                outboxProcessingService.markPublished(event.getId(), event.getProcessingToken());
                meetingMetrics.recordKafkaEventPublished(event.getEventType(), topic);
            } catch (Exception ex) {
                outboxProcessingService.markFailed(event.getId(), event.getProcessingToken());
                log.error("Cannot publish outbox event {}", event.getId(), ex);
            }
        }
    }

    private String resolveTopic(EventType eventType) {
        return switch (eventType) {
            case MEETING_CREATED -> topicsProperties.getMeetingCreated();
            case MEETING_UPDATED -> topicsProperties.getMeetingUpdated();
            case MEETING_CANCELLED -> topicsProperties.getMeetingCancelled();
            case MEETING_PARTICIPANT_ADDED -> topicsProperties.getMeetingParticipantAdded();
            case MEETING_PARTICIPANT_REMOVED -> topicsProperties.getMeetingParticipantRemoved();
        };
    }
}
