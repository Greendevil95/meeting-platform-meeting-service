package com.example.meetingservice.kafka.user;

import com.example.meetingservice.entity.UserStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserStatusChangedEvent(
        UUID eventId,
        UUID userId,
        UserStatus previousStatus,
        UserStatus status,
        OffsetDateTime timestamp
) {
}
