package com.example.meetingservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "meetings")
public class MeetingEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "organizer_id", nullable = false)
    private UUID organizerId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MeetingStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingParticipantEntity> participants = new ArrayList<>();

    public void addParticipant(UUID userId, ParticipantRole role) {
        var participant = new MeetingParticipantEntity();
        participant.setId(new MeetingParticipantId(id, userId));
        participant.setMeeting(this);
        participant.setRole(role);
        participant.setResponseStatus(ResponseStatus.PENDING);
        participants.add(participant);
    }

    public void removeParticipant(UUID userId) {
        participants.removeIf(participant -> participant.getId().getUserId().equals(userId));
    }
}
