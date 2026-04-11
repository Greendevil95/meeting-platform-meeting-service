package com.example.meetingservice.kafka.meeting;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingCancelledEvent(
        UUID eventId,
        OffsetDateTime occurredAt,
        UUID meetingId,
        Long requestorId
) implements MeetingEvent {
    @Override
    public MeetingEventType getEventType() {
        return MeetingEventType.MEETING_CANCELLED;
    }
}
