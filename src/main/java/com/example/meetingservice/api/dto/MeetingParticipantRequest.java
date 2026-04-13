package com.example.meetingservice.api.dto;

import com.example.meetingservice.entity.ParticipantRole;

import java.util.UUID;

public record MeetingParticipantRequest(
        UUID userId,
        ParticipantRole role
) {
}
