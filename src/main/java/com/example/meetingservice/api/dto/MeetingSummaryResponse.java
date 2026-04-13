package com.example.meetingservice.api.dto;

import com.example.meetingservice.entity.MeetingStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingSummaryResponse(
        UUID id,
        String title,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        MeetingStatus status
) {
}
