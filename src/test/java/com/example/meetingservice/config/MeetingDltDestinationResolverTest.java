package com.example.meetingservice.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeetingDltDestinationResolverTest {

    private final MeetingDltDestinationResolver resolver =
            new MeetingDltDestinationResolver("meeting-service");

    @Test
    void resolvesUserEventToServiceSpecificDltTopic() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("user-service.user-status-changed", 2, 42L, "user-id", "{}");

        TopicPartition destination = resolver.apply(record, new IllegalStateException("boom"));

        assertEquals("meeting-service.dlt.user-status-changed", destination.topic());
        assertEquals(2, destination.partition());
    }
}
