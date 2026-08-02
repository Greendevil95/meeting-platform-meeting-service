package com.example.meetingservice.service;

import com.example.meetingservice.entity.MeetingEntity;
import com.example.meetingservice.entity.MeetingParticipantEntity;
import com.example.meetingservice.entity.MeetingParticipantId;
import com.example.meetingservice.entity.ParticipantRole;
import com.example.meetingservice.exception.ForbiddenException;
import com.example.meetingservice.repository.MeetingParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class MeetingValidationServiceTest {

    @Mock
    private MeetingParticipantRepository participantRepository;

    @Mock
    private UserReadModelService userReadModelService;

    private MeetingValidationService service;

    @BeforeEach
    void setUp() {
        service = new MeetingValidationService(participantRepository, userReadModelService);
    }

    @Test
    void allowsOrganizerToManageMeeting() {
        UUID organizerId = UUID.randomUUID();
        MeetingEntity meeting = meeting(organizerId);

        assertDoesNotThrow(() -> service.assertCanManageMeeting(meeting, new CurrentUser(organizerId, false)));
    }

    @Test
    void allowsAdminToManageAnotherUsersMeeting() {
        MeetingEntity meeting = meeting(UUID.randomUUID());

        assertDoesNotThrow(() -> service.assertCanManageMeeting(meeting, new CurrentUser(UUID.randomUUID(), true)));
    }

    @Test
    void rejectsNonOrganizerFromManagingMeeting() {
        MeetingEntity meeting = meeting(UUID.randomUUID());

        assertThrows(
                ForbiddenException.class,
                () -> service.assertCanManageMeeting(meeting, new CurrentUser(UUID.randomUUID(), false))
        );
    }

    @Test
    void allowsParticipantToViewMeeting() {
        UUID participantId = UUID.randomUUID();
        MeetingEntity meeting = meeting(UUID.randomUUID());
        MeetingParticipantEntity participant = new MeetingParticipantEntity();
        participant.setId(new MeetingParticipantId(meeting.getId(), participantId));
        participant.setRole(ParticipantRole.ATTENDEE);

        assertDoesNotThrow(() -> service.assertCanViewMeeting(
                meeting,
                List.of(participant),
                new CurrentUser(participantId, false)
        ));
    }

    @Test
    void rejectsNonParticipantFromViewingMeeting() {
        MeetingEntity meeting = meeting(UUID.randomUUID());

        assertThrows(
                ForbiddenException.class,
                () -> service.assertCanViewMeeting(meeting, List.of(), new CurrentUser(UUID.randomUUID(), false))
        );
    }

    private MeetingEntity meeting(UUID organizerId) {
        MeetingEntity meeting = new MeetingEntity();
        meeting.setId(UUID.randomUUID());
        meeting.setOrganizerId(organizerId);
        return meeting;
    }
}
