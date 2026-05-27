package com.example.meetingservice.kafka.meeting;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingCreatedEvent(
        UUID eventId,
        OffsetDateTime occurredAt,
        Long version,
        UUID meetingId,
        UUID organizerId,
        String title,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt
) implements MeetingEvent {
    @Override
    public EventType getEventType() {
        return EventType.MEETING_CREATED;
    }
}
