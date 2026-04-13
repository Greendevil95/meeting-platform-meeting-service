package com.example.meetingservice.api.dto;

import com.example.meetingservice.entity.MeetingStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MeetingResponse(
        UUID id,
        UUID organizerId,
        String title,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        MeetingStatus status,
        List<MeetingParticipantRequest> participants
) {
}
