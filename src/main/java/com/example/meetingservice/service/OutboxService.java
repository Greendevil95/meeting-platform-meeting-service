package com.example.meetingservice.service;

import com.example.meetingservice.entity.OutboxEventEntity;
import com.example.meetingservice.entity.OutboxStatus;
import com.example.meetingservice.kafka.meeting.MeetingEvent;
import com.example.meetingservice.repository.OutboxEventRepository;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

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
        outboxEvent.setEventJson(toJson(event));
        outboxEventRepository.save(outboxEvent);
    }

    private String toJson(MeetingEvent eventPayload) {
        try {
            return objectMapper.writeValueAsString(eventPayload);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Cannot serialize outbox event", ex);
        }
    }
}
