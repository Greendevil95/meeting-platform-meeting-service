package com.example.meetingservice.kafka.meeting;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingUpdatedEvent(
        UUID eventId,
        OffsetDateTime occurredAt,
        UUID meetingId,
        String title,
        OffsetDateTime startAt,
        OffsetDateTime endAt
) implements MeetingEvent {
    @Override
    public MeetingEventType getEventType() {
        return MeetingEventType.MEETING_UPDATED;
    }
}
