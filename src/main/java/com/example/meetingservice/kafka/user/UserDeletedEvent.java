package com.example.meetingservice.kafka.user;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserDeletedEvent(
        UUID eventId,
        UUID userId,
        OffsetDateTime timestamp
) {
}
