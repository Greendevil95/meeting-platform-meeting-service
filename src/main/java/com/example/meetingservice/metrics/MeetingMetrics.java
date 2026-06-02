package com.example.meetingservice.metrics;

import com.example.meetingservice.kafka.meeting.EventType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

@Component
public class MeetingMetrics {

    private static final String MEETINGS_CREATED = "app.meetings.created";
    private static final String MEETINGS_UPDATED = "app.meetings.updated";
    private static final String MEETINGS_CANCELLED = "app.meetings.cancelled";
    private static final String MEETING_PARTICIPANTS_ADDED = "app.meeting_participants.added";
    private static final String MEETING_PARTICIPANTS_REMOVED = "app.meeting_participants.removed";
    private static final String KAFKA_EVENTS_PUBLISHED = "app.kafka.events.published";
    private static final String KAFKA_EVENTS_SKIPPED = "app.kafka.events.skipped";
    private static final String KAFKA_EVENTS_DLT_ROUTED = "app.kafka.events.dlt.routed";

    private final MeterRegistry meterRegistry;

    public MeetingMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordMeetingCreated() {
        meterRegistry.counter(MEETINGS_CREATED).increment();
    }

    public void recordMeetingUpdated() {
        meterRegistry.counter(MEETINGS_UPDATED).increment();
    }

    public void recordMeetingCancelled() {
        meterRegistry.counter(MEETINGS_CANCELLED).increment();
    }

    public void recordParticipantsAdded(int count) {
        if (count > 0) {
            meterRegistry.counter(MEETING_PARTICIPANTS_ADDED).increment(count);
        }
    }

    public void recordParticipantRemoved() {
        meterRegistry.counter(MEETING_PARTICIPANTS_REMOVED).increment();
    }

    public void recordKafkaEventPublished(EventType eventType, String topic) {
        meterRegistry.counter(
                KAFKA_EVENTS_PUBLISHED,
                Tags.of("event_type", eventType.name(), "topic", topic)
        ).increment();
    }

    public void recordKafkaEventSkipped(String readModel, String reason) {
        meterRegistry.counter(KAFKA_EVENTS_SKIPPED, Tags.of("read_model", readModel, "reason", reason)).increment();
    }

    public void recordDltRouted(String originalTopic, String dltTopic, Exception exception) {
        meterRegistry.counter(
                KAFKA_EVENTS_DLT_ROUTED,
                Tags.of(
                        "original_topic", originalTopic,
                        "dlt_topic", dltTopic,
                        "exception", exception.getClass().getSimpleName()
                )
        ).increment();
    }
}
