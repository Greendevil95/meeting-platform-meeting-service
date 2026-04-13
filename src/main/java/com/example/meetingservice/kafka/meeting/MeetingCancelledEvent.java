package com.example.meetingservice.kafka.meeting;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingCancelledEvent(
        UUID eventId,
        OffsetDateTime occurredAt,
        UUID meetingId,
        UUID requestorId
) implements MeetingEvent {
    @Override
    public EventType getEventType() {
        return EventType.MEETING_CANCELLED;
    }
}
