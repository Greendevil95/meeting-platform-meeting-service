package com.example.meetingservice.kafka.meeting;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingParticipantRemovedEvent(
        UUID eventId,
        OffsetDateTime occurredAt,
        Long version,
        UUID meetingId,
        UUID userId
) implements MeetingEvent {
    @Override
    public EventType getEventType() {
        return EventType.MEETING_PARTICIPANT_REMOVED;
    }
}
