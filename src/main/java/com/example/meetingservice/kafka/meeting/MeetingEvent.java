package com.example.meetingservice.kafka.meeting;

import java.util.UUID;

public interface MeetingEvent {
    EventType getEventType();
    UUID eventId();
}
