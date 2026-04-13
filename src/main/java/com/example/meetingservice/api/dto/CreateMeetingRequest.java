package com.example.meetingservice.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CreateMeetingRequest(
        @NotNull UUID organizerUserId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 1000) String description,
        @NotNull @Future OffsetDateTime startAt,
        @NotNull @Future OffsetDateTime endAt,
        @NotEmpty List<@NotNull UUID> participantUserIds
) {
}
