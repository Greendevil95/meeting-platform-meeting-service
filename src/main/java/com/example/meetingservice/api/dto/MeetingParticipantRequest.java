package com.example.meetingservice.api.dto;

import com.example.meetingservice.domain.ParticipantRole;

public record MeetingParticipantRequest(
        Long userId,
        ParticipantRole role
) {
}
