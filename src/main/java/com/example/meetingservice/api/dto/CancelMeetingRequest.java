package com.example.meetingservice.api.dto;

import jakarta.validation.constraints.NotNull;

public record CancelMeetingRequest(@NotNull Long requestorId) {
}
