package com.example.meetingservice.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaRetryPropertiesTest {

    @Test
    void defaultsToThreeExponentialBackoffRetries() {
        KafkaRetryProperties properties = new KafkaRetryProperties();

        assertEquals(3, properties.getMaxRetries());
        assertEquals(Duration.ofSeconds(1), properties.getInitialInterval());
        assertEquals(2.0, properties.getMultiplier());
        assertEquals(Duration.ofSeconds(10), properties.getMaxInterval());
    }
}
