package com.example.meetingservice.service;

import com.example.meetingservice.api.dto.*;
import com.example.meetingservice.config.CacheConfig;
import com.example.meetingservice.entity.MeetingStatus;
import com.example.meetingservice.entity.MeetingEntity;
import com.example.meetingservice.entity.MeetingParticipantEntity;
import com.example.meetingservice.entity.MeetingParticipantId;
import com.example.meetingservice.exception.BadRequestException;
import com.example.meetingservice.exception.ConflictException;
import com.example.meetingservice.exception.NotFoundException;
import com.example.meetingservice.kafka.meeting.MeetingCancelledEvent;
import com.example.meetingservice.kafka.meeting.MeetingCreatedEvent;
import com.example.meetingservice.kafka.meeting.MeetingParticipantAddedEvent;
import com.example.meetingservice.kafka.meeting.MeetingParticipantRemovedEvent;
import com.example.meetingservice.kafka.meeting.MeetingUpdatedEvent;
import com.example.meetingservice.repository.MeetingParticipantRepository;
import com.example.meetingservice.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.meetingservice.entity.ParticipantRole.ATTENDEE;
import static com.example.meetingservice.entity.ParticipantRole.ORGANIZER;

@Slf4j
@RequiredArgsConstructor
@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final MeetingQueryService meetingQueryService;
    private final MeetingValidationService meetingValidationService;
    private final OutboxService outboxService;
    private final MeetingMapper meetingMapper;

    @Transactional
    public MeetingResponse create(CreateMeetingRequest request) {
        meetingValidationService.validateTimeRange(request.startAt(), request.endAt());
        meetingValidationService.validateUsersForMeeting(request.participantUserIds(), request.organizerUserId());
        meetingValidationService.ensureNoScheduleConflicts(request.participantUserIds(), request.startAt(), request.endAt(), null);

        MeetingEntity meeting = meetingMapper.toEntity(request);
        meetingRepository.save(meeting);

        var participantEntities = new ArrayList<MeetingParticipantEntity>();
        participantEntities.add(
                meetingMapper.toParticipantEntity(meeting.getId(), new MeetingParticipantRequest(request.organizerUserId(), ORGANIZER))
        );
        participantEntities.addAll(
                meetingMapper.toParticipantEntities(meeting.getId(), ATTENDEE, request.participantUserIds())
        );
        participantRepository.saveAll(participantEntities);

        outboxService.enqueueEvent(
                "MEETING",
                meeting.getId().toString(),
                new MeetingCreatedEvent(
                        UUID.randomUUID(),
                        OffsetDateTime.now(),
                        meeting.getId(),
                        meeting.getOrganizerId(),
                        meeting.getTitle(),
                        meeting.getDescription(),
                        meeting.getStartAt(),
                        meeting.getEndAt()
                )
        );
        participantEntities.stream()
                .filter(participant -> participant.getRole() == ATTENDEE)
                .forEach(participant -> outboxService.enqueueEvent(
                        "MEETING",
                        meeting.getId().toString(),
                        new MeetingParticipantAddedEvent(
                                UUID.randomUUID(),
                                OffsetDateTime.now(),
                                meeting.getId(),
                                participant.getId().getUserId()
                        )
                ));
        return meetingMapper.toResponse(meeting, participantEntities);
    }

    @CachePut(value = CacheConfig.MEETING_CACHE_NAME, key = "#meetingId")
    @Transactional
    public MeetingResponse update(UUID meetingId, UpdateMeetingRequest request) {
        var meeting = meetingQueryService.loadActiveMeeting(meetingId);
        meetingValidationService.assertOrganizer(meeting, request.organizerUserId());

        meetingValidationService.validateTimeRange(request.startAt(), request.endAt());

        var participants = participantRepository.findAllByIdMeetingId(meetingId);
        Set<UUID> userIds = participants.stream()
                .map(p -> p.getId().getUserId())
                .collect(Collectors.toSet());
        meetingValidationService.ensureNoScheduleConflicts(userIds, request.startAt(), request.endAt(), meetingId);

        meetingMapper.updateEntity(request, meeting);
        meetingRepository.save(meeting);

        outboxService.enqueueEvent(
                "MEETING",
                meeting.getId().toString(),
                new MeetingUpdatedEvent(
                        UUID.randomUUID(),
                        OffsetDateTime.now(),
                        meeting.getId(),
                        meeting.getOrganizerId(),
                        userIds,
                        meeting.getTitle(),
                        meeting.getDescription(),
                        meeting.getStartAt(),
                        meeting.getEndAt()
                )
        );
        return meetingMapper.toResponse(meeting, participants);
    }

    @CacheEvict(value = CacheConfig.MEETING_CACHE_NAME, key = "#meetingId")
    @Transactional
    public void cancel(UUID meetingId, CancelMeetingRequest request) {
        var meeting = meetingQueryService.loadActiveMeeting(meetingId);
        meetingValidationService.assertOrganizer(meeting, request.requestorId());
        meeting.setStatus(MeetingStatus.CANCELLED);
        meetingRepository.save(meeting);

        outboxService.enqueueEvent(
                "MEETING",
                meeting.getId().toString(),
                new MeetingCancelledEvent(
                        UUID.randomUUID(),
                        OffsetDateTime.now(),
                        meeting.getId(),
                        request.requestorId()
                )
        );
    }

    @Transactional
    public MeetingResponse addParticipant(UUID meetingId, AddParticipantRequest request) {
        MeetingEntity meeting = meetingQueryService.loadActiveMeeting(meetingId);
        meetingValidationService.assertOrganizer(meeting, request.requestorId());
        var userId = request.participant().userId();
        meetingValidationService.validateUserActive(userId);

        MeetingParticipantId participantId = new MeetingParticipantId(meetingId, userId);
        if (participantRepository.existsById(participantId)) {
            throw new ConflictException("Participant already exists in meeting");
        }

        meetingValidationService.ensureNoScheduleConflicts(
                Set.of(userId),
                meeting.getStartAt(),
                meeting.getEndAt(),
                meetingId
        );
        var participantEntity = meetingMapper.toParticipantEntity(
                meeting.getId(),
                request.participant()
        );
        participantRepository.save(participantEntity);
        outboxService.enqueueEvent(
                "MEETING",
                meeting.getId().toString(),
                new MeetingParticipantAddedEvent(
                        UUID.randomUUID(),
                        OffsetDateTime.now(),
                        meeting.getId(),
                        userId
                )
        );
        return meetingQueryService.getById(meetingId);
    }

    @Transactional
    public MeetingResponse removeParticipant(UUID meetingId, UUID userId, UUID requestorId) {
        MeetingEntity meeting = meetingQueryService.loadActiveMeeting(meetingId);
        meetingValidationService.assertOrganizer(meeting, requestorId);
        if (meeting.getOrganizerId().equals(userId)) {
            throw new BadRequestException(
                    "error.organizer.cannot.be.removed",
                    "Organizer cannot be removed from participants"
            );
        }
        MeetingParticipantId participantId = new MeetingParticipantId(meetingId, userId);
        if (!participantRepository.existsById(participantId)) {
            throw new NotFoundException("Participant not found");
        }
        participantRepository.deleteById(participantId);

        outboxService.enqueueEvent(
                "MEETING",
                meeting.getId().toString(),
                new MeetingParticipantRemovedEvent(
                        UUID.randomUUID(),
                        OffsetDateTime.now(),
                        meeting.getId(),
                        userId
                )
        );
        return meetingQueryService.getById(meetingId);
    }
}
