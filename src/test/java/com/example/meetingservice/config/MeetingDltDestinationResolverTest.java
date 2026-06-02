package com.example.meetingservice.config;

import com.example.meetingservice.metrics.MeetingMetrics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MeetingDltDestinationResolverTest {

    private final MeetingMetrics meetingMetrics = mock(MeetingMetrics.class);
    private final MeetingDltDestinationResolver resolver =
            new MeetingDltDestinationResolver("meeting-service", meetingMetrics);

    @Test
    void resolvesUserEventToServiceSpecificDltTopic() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("user-service.user-status-changed", 2, 42L, "user-id", "{}");
        IllegalStateException exception = new IllegalStateException("boom");

        TopicPartition destination = resolver.apply(record, exception);

        assertEquals("meeting-service.dlt.user-status-changed", destination.topic());
        assertEquals(2, destination.partition());
        verify(meetingMetrics).recordDltRouted(
                "user-service.user-status-changed",
                "meeting-service.dlt.user-status-changed",
                exception
        );
    }
}
