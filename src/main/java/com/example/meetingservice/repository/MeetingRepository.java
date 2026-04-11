package com.example.meetingservice.repository;

import com.example.meetingservice.domain.MeetingStatus;
import com.example.meetingservice.entity.MeetingEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingRepository extends JpaRepository<MeetingEntity, UUID> {

    Optional<MeetingEntity> findByIdAndStatus(UUID id, MeetingStatus status);

    @Query(value = """
            select distinct m.*
            from meetings m
            join meeting_participants mp on mp.meeting_id = m.id
            where mp.user_id = :userId
              and m.status = 'SCHEDULED'
              and m.start_at < :toTime
              and m.end_at > :fromTime
            order by m.start_at asc
            """, nativeQuery = true)
    List<MeetingEntity> findUserMeetings(
            @Param("userId") Long userId,
            @Param("fromTime") OffsetDateTime fromTime,
            @Param("toTime") OffsetDateTime toTime
    );
}
