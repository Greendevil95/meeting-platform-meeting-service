package com.example.meetingservice.repository;

import com.example.meetingservice.entity.MeetingParticipantEntity;
import com.example.meetingservice.entity.MeetingParticipantId;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipantEntity, MeetingParticipantId> {

    List<MeetingParticipantEntity> findAllByIdMeetingId(UUID meetingId);

    @Query(value = """
            select count(distinct m.id)
            from meetings m
            join meeting_participants mp on mp.meeting_id = m.id
            where m.status = 'SCHEDULED'
              and mp.user_id in (:userIds)
              and m.start_at < :endAt
              and m.end_at > :startAt
            """, nativeQuery = true)
    long countConflicts(
            @Param("userIds") Collection<UUID> userIds,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt
    );

    @Query(value = """
            select count(distinct m.id)
            from meetings m
            join meeting_participants mp on mp.meeting_id = m.id
            where m.status = 'SCHEDULED'
              and m.id <> :meetingId
              and mp.user_id in (:userIds)
              and m.start_at < :endAt
              and m.end_at > :startAt
            """, nativeQuery = true)
    long countConflictsExcludingMeeting(
            @Param("meetingId") UUID meetingId,
            @Param("userIds") Collection<UUID> userIds,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt
    );
}
