package com.example.meetingservice.metrics;

import com.example.meetingservice.kafka.meeting.EventType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeetingMetricsTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final MeetingMetrics meetingMetrics = new MeetingMetrics(meterRegistry);

    @Test
    void recordsMeetingBusinessCounters() {
        meetingMetrics.recordMeetingCreated();
        meetingMetrics.recordMeetingUpdated();
        meetingMetrics.recordMeetingCancelled();
        meetingMetrics.recordParticipantsAdded(2);
        meetingMetrics.recordParticipantRemoved();

        assertEquals(1.0, meterRegistry.counter("app.meetings.created").count());
        assertEquals(1.0, meterRegistry.counter("app.meetings.updated").count());
        assertEquals(1.0, meterRegistry.counter("app.meetings.cancelled").count());
        assertEquals(2.0, meterRegistry.counter("app.meeting_participants.added").count());
        assertEquals(1.0, meterRegistry.counter("app.meeting_participants.removed").count());
    }

    @Test
    void recordsPublishedKafkaEventWithStableTags() {
        meetingMetrics.recordKafkaEventPublished(EventType.MEETING_CREATED, "meeting-service.meeting-created");

        assertEquals(
                1.0,
                meterRegistry.counter(
                        "app.kafka.events.published",
                        "event_type", "MEETING_CREATED",
                        "topic", "meeting-service.meeting-created"
                ).count()
        );
    }

    @Test
    void recordsSkippedKafkaEvent() {
        meetingMetrics.recordKafkaEventSkipped("user", "stale_version");

        assertEquals(
                1.0,
                meterRegistry.counter(
                        "app.kafka.events.skipped",
                        "read_model", "user",
                        "reason", "stale_version"
                ).count()
        );
    }

    @Test
    void recordsDltRoutedWithoutExceptionMessage() {
        meetingMetrics.recordDltRouted(
                "user-service.user-updated",
                "meeting-service.dlt.user-updated",
                new IllegalStateException("dynamic payload")
        );

        assertEquals(
                1.0,
                meterRegistry.counter(
                        "app.kafka.events.dlt.routed",
                        "original_topic", "user-service.user-updated",
                        "dlt_topic", "meeting-service.dlt.user-updated",
                        "exception", "IllegalStateException"
                ).count()
        );
    }
}
