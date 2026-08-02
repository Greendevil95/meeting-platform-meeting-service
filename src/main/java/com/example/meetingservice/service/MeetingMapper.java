package com.example.meetingservice.service;

import com.example.meetingservice.api.dto.*;
import com.example.meetingservice.entity.ParticipantRole;
import com.example.meetingservice.entity.ResponseStatus;
import com.example.meetingservice.entity.MeetingEntity;
import com.example.meetingservice.entity.MeetingParticipantEntity;
import com.example.meetingservice.entity.MeetingParticipantId;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MeetingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "SCHEDULED")
    @Mapping(target = "organizerId", ignore = true)
    MeetingEntity toEntity(CreateMeetingRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateMeetingRequest request, @MappingTarget MeetingEntity meeting);

    @Mapping(target = "userId", source = "id.userId")
    MeetingParticipantRequest toParticipantDto(MeetingParticipantEntity participant);

    default MeetingParticipantEntity toParticipantEntity(MeetingEntity meeting,
                                                         MeetingParticipantRequest dto) {
        var meetingParticipant = new MeetingParticipantEntity();
        meetingParticipant.setRole(dto.role());
        meetingParticipant.setId(new MeetingParticipantId(meeting.getId(), dto.userId()));
        meetingParticipant.setMeeting(meeting);
        meetingParticipant.setResponseStatus(ResponseStatus.PENDING);
        return meetingParticipant;
    }

    default MeetingParticipantEntity toParticipantEntity(MeetingEntity meeting,
                                                         UUID userId,
                                                         ParticipantRole role) {
        var meetingParticipant = new MeetingParticipantEntity();
        meetingParticipant.setRole(role);
        meetingParticipant.setId(new MeetingParticipantId(meeting.getId(), userId));
        meetingParticipant.setMeeting(meeting);
        meetingParticipant.setResponseStatus(ResponseStatus.PENDING);
        return meetingParticipant;
    }

    default List<MeetingParticipantEntity> toParticipantEntities(
            MeetingEntity meeting,
            List<MeetingParticipantRequest> users
    ) {
        return users.stream()
                .map(user -> toParticipantEntity(meeting, user))
                .toList();
    }

    default List<MeetingParticipantEntity> toParticipantEntities(
            MeetingEntity meeting,
            ParticipantRole role,
            List<UUID> userIds
    ) {
        return userIds.stream()
                .map(userId -> toParticipantEntity(meeting, userId, role))
                .toList();
    }

    @Mapping(target = "participants", source = "participants")
    MeetingResponse toResponse(MeetingEntity meeting, List<MeetingParticipantEntity> participants);

    MeetingSummaryResponse toSummary(MeetingEntity meeting);
}
