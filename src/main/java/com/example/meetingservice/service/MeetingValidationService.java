package com.example.meetingservice.service;

import com.example.meetingservice.domain.UserStatus;
import com.example.meetingservice.entity.MeetingEntity;
import com.example.meetingservice.exception.BadRequestException;
import com.example.meetingservice.exception.ConflictException;
import com.example.meetingservice.exception.ForbiddenException;
import com.example.meetingservice.exception.NotFoundException;
import com.example.meetingservice.repository.MeetingParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class MeetingValidationService {
    private final MeetingParticipantRepository participantRepository;
    private final UserReadModelService userReadModelService;

    public void assertOrganizer(MeetingEntity meeting, Long requestorId) {
        if (!meeting.getOrganizerId().equals(requestorId)) {
            throw new ForbiddenException("Only organizer can modify meeting");
        }
    }

    public void validateTimeRange(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (!startAt.isBefore(endAt)) {
            throw new BadRequestException("error.invalid.time.range", "startAt must be before endAt");
        }
    }

    public void validateUsersActive(List<Long> userIds) {
        for (var id : userIds) {
            validateUserActive(id);
        }
    }

    public void validateUserActive(Long userId) {
        var profile = userReadModelService.findUser(userId)
                .orElseThrow(() -> new NotFoundException("Unknown user: " + userId));
        if (profile.status() == UserStatus.DELETED) {
            throw new BadRequestException("error.user.deleted", "User is deleted: " + userId);
        }
        if (profile.status() == UserStatus.INACTIVE) {
            throw new BadRequestException("error.user.inactive", "User is inactive: " + userId);
        }
    }

    public void validateUsersForMeeting(List<Long> participantIds, Long organizerId) {
        if (organizerId == null) {
            throw new BadRequestException("Organizer is missing");
        }

        if (participantIds.contains(organizerId)) {
            throw new BadRequestException("Organizer must not be in participants list");
        }

        validateUsersActive(
                Stream.concat(
                        Stream.of(organizerId),
                        participantIds.stream()
                ).toList()
        );
    }

    public void ensureNoScheduleConflicts(Collection<Long> userIds, OffsetDateTime startAt, OffsetDateTime endAt, UUID excludedMeetingId) {
        long conflicts = excludedMeetingId == null
                ? participantRepository.countConflicts(userIds, startAt, endAt)
                : participantRepository.countConflictsExcludingMeeting(excludedMeetingId, userIds, startAt, endAt);
        if (conflicts > 0) {
            throw new ConflictException("Schedule conflict detected");
        }
    }
}
