package com.example.meetingservice.kafka.user;

import com.example.meetingservice.domain.UserStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserStatusChangedEvent(
        UUID eventId,
        Long userId,
        UserStatus previousStatus,
        UserStatus status,
        OffsetDateTime timestamp
) {
}
