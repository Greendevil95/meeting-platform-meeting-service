package com.example.meetingservice.kafka.meeting;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingCreatedEvent(
        UUID eventId,
        OffsetDateTime occurredAt,
        UUID meetingId,
        Long organizerId,
        String title,
        OffsetDateTime startAt,
        OffsetDateTime endAt
) implements MeetingEvent {
    @Override
    public MeetingEventType getEventType() {
        return MeetingEventType.MEETING_CREATED;
    }
}
