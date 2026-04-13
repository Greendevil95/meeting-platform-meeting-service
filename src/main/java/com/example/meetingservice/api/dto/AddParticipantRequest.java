package com.example.meetingservice.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddParticipantRequest(
        @NotNull UUID requestorId,
        @NotNull MeetingParticipantRequest participant
) {
}
