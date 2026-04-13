package com.example.meetingservice.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CancelMeetingRequest(@NotNull UUID requestorId) {
}
