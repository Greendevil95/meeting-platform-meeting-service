package com.example.meetingservice.service;

import com.example.meetingservice.domain.UserRole;
import com.example.meetingservice.domain.UserStatus;
import java.time.OffsetDateTime;

public record UserProfile(
        Long userId,
        String username,
        String email,
        UserStatus status,
        UserRole role,
        OffsetDateTime updatedAt
) {
}
