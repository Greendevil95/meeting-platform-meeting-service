package com.example.meetingservice.kafka.user;

import com.example.meetingservice.domain.UserRole;
import com.example.meetingservice.domain.UserStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserUpdatedEvent(
        UUID eventId,
        Long userId,
        String username,
        String email,
        UserStatus status,
        UserRole role,
        OffsetDateTime timestamp
) {
}
