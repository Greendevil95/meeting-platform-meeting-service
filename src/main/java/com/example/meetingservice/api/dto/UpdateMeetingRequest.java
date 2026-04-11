package com.example.meetingservice.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record UpdateMeetingRequest(
        @NotNull Long organizerUserId,
        @Size(max = 255) String title,
        @Size(max = 1000) String description,
        @Future OffsetDateTime startAt,
        @Future OffsetDateTime endAt
) {
}
