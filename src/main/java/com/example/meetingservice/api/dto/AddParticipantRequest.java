package com.example.meetingservice.api.dto;

import jakarta.validation.constraints.NotNull;

public record AddParticipantRequest(
        @NotNull Long requestorId,
        @NotNull MeetingParticipantRequest participant
) {
}
