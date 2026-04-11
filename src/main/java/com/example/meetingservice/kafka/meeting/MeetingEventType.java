package com.example.meetingservice.kafka.meeting;

public enum MeetingEventType {
    MEETING_CREATED(MeetingCreatedEvent.class),
    MEETING_UPDATED(MeetingUpdatedEvent.class),
    MEETING_CANCELLED(MeetingCancelledEvent.class),
    MEETING_PARTICIPANT_ADDED( MeetingParticipantAddedEvent.class),
    MEETING_PARTICIPANT_REMOVED(MeetingParticipantRemovedEvent.class);

    private final Class<? extends MeetingEvent> eventClass;

    MeetingEventType(Class<? extends MeetingEvent> eventClass) {
        this.eventClass = eventClass;
    }

    public Class<? extends MeetingEvent> eventClass() {
        return eventClass;
    }
}