package com.example.meetingservice.kafka.user;

import com.example.meetingservice.entity.UserRole;
import com.example.meetingservice.entity.UserStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserCreatedEvent(
        UUID eventId,
        UUID userId,
        String username,
        String email,
        UserStatus status,
        UserRole role,
        OffsetDateTime timestamp
) {
}
