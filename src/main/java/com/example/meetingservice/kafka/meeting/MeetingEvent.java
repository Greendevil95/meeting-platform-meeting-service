package com.example.meetingservice.kafka.meeting;

import java.util.UUID;

public interface MeetingEvent {
    MeetingEventType getEventType();
    UUID eventId();
}
