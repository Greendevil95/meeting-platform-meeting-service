package com.example.meetingservice.service;

import com.example.meetingservice.entity.OutboxEventEntity;
import com.example.meetingservice.entity.OutboxStatus;
import com.example.meetingservice.kafka.meeting.MeetingEvent;
import com.example.meetingservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;

    public void enqueueEvent(String aggregateType,
                             String aggregateId,
                             MeetingEvent event) {
        OutboxEventEntity outboxEvent = new OutboxEventEntity();
        outboxEvent.setEventId(event.eventId());
        outboxEvent.setAggregateType(aggregateType);
        outboxEvent.setAggregateId(aggregateId);
        outboxEvent.setEventType(event.getEventType());
        outboxEvent.setStatus(OutboxStatus.PENDING);
        outboxEvent.setRetryCount(0);
        outboxEvent.setCreatedAt(OffsetDateTime.now());
        outboxEvent.setEventJson(toMap(event));
        outboxEventRepository.save(outboxEvent);
    }

    private Map<String, Object> toMap(MeetingEvent eventPayload) {
        try {
            return jsonMapper.convertValue(
                    eventPayload,
                    new TypeReference<>() {}
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot serialize outbox event", ex);
        }
    }
}
