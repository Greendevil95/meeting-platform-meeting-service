package com.example.meetingservice.kafka.meeting;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingParticipantAddedEvent(
        UUID eventId,
        OffsetDateTime occurredAt,
        UUID meetingId,
        Long userId
) implements MeetingEvent {
    @Override
    public MeetingEventType getEventType() {
        return MeetingEventType.MEETING_PARTICIPANT_ADDED;
    }
}
