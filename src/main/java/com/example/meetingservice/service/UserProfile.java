package com.example.meetingservice.service;

import com.example.meetingservice.entity.UserRole;
import com.example.meetingservice.entity.UserStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfile(
        UUID userId,
        String username,
        String email,
        UserStatus status,
        UserRole role,
        OffsetDateTime updatedAt
) {
}
