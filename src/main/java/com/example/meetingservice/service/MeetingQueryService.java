package com.example.meetingservice.service;

import com.example.meetingservice.api.dto.MeetingResponse;
import com.example.meetingservice.api.dto.MeetingSummaryResponse;
import com.example.meetingservice.config.CacheConfig;
import com.example.meetingservice.domain.MeetingStatus;
import com.example.meetingservice.entity.MeetingEntity;
import com.example.meetingservice.entity.MeetingParticipantEntity;
import com.example.meetingservice.exception.NotFoundException;
import com.example.meetingservice.repository.MeetingParticipantRepository;
import com.example.meetingservice.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MeetingQueryService {
    private final MeetingParticipantRepository participantRepository;
    private final MeetingMapper meetingMapper;
    private final MeetingRepository meetingRepository;
    private final MeetingValidationService meetingValidationService;

    @Transactional(readOnly = true)
    public List<MeetingSummaryResponse> list(Long userId, OffsetDateTime from, OffsetDateTime to) {
        meetingValidationService.validateTimeRange(from, to);
        return meetingRepository.findUserMeetings(userId, from, to)
                .stream()
                .map(meetingMapper::toSummary)
                .toList();
    }

    @Cacheable(value = CacheConfig.MEETING_CACHE_NAME, key = "#meetingId")
    @Transactional(readOnly = true)
    public MeetingResponse getById(UUID meetingId) {
        MeetingEntity meeting = loadActiveMeeting(meetingId);
        List<MeetingParticipantEntity> participants = participantRepository.findAllByIdMeetingId(meetingId);
        return meetingMapper.toResponse(meeting, participants);
    }

    public MeetingEntity loadActiveMeeting(UUID meetingId) {
        return meetingRepository.findByIdAndStatus(meetingId, MeetingStatus.SCHEDULED)
                .orElseThrow(() -> new NotFoundException("Meeting not found"));
    }
}
