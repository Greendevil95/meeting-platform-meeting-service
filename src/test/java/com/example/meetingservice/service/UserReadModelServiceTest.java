package com.example.meetingservice.service;

import com.example.meetingservice.entity.UserReadModelEntity;
import com.example.meetingservice.entity.UserRole;
import com.example.meetingservice.entity.UserStatus;
import com.example.meetingservice.metrics.MeetingMetrics;
import com.example.meetingservice.repository.UserReadModelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserReadModelServiceTest {

    @Mock
    private UserReadModelRepository repository;

    @Mock
    private UserReadModelMapper userReadModelMapper;

    @Mock
    private MeetingMetrics meetingMetrics;

    private UserReadModelService service;

    @BeforeEach
    void setUp() {
        service = new UserReadModelService(repository, userReadModelMapper, meetingMetrics);
    }

    @Test
    void upsertUserSkipsOlderOrDuplicateVersion() {
        UUID userId = UUID.randomUUID();
        UserReadModelEntity existing = existingUser(userId, 5L);
        UserProfile staleProfile = new UserProfile(
                userId,
                "alice-stale",
                "stale@test.local",
                UserStatus.ACTIVE,
                UserRole.USER,
                5L,
                OffsetDateTime.now().plusDays(1)
        );

        when(repository.findById(userId)).thenReturn(Optional.of(existing));

        service.upsertUser(staleProfile);

        verify(meetingMetrics).recordKafkaEventSkipped("user", "stale_version");
        verify(userReadModelMapper, never()).updateEntity(staleProfile, existing);
        verify(repository, never()).save(existing);
    }

    @Test
    void updateStatusSkipsOlderOrDuplicateVersion() {
        UUID userId = UUID.randomUUID();
        UserReadModelEntity existing = existingUser(userId, 7L);

        when(repository.findById(userId)).thenReturn(Optional.of(existing));

        service.updateStatus(userId, UserStatus.DELETED, 7L, OffsetDateTime.now().plusDays(1));

        verify(meetingMetrics).recordKafkaEventSkipped("user", "stale_version");
        verify(repository, never()).save(existing);
    }

    @Test
    void updateStatusCreatesPlaceholderForUnknownUser() {
        UUID userId = UUID.randomUUID();
        OffsetDateTime eventTime = OffsetDateTime.now();
        when(repository.findById(userId)).thenReturn(Optional.empty());

        service.updateStatus(userId, UserStatus.DELETED, 1L, eventTime);

        verify(repository).save(any(UserReadModelEntity.class));
        verify(meetingMetrics, never()).recordKafkaEventSkipped("user", "stale_version");
    }

    private UserReadModelEntity existingUser(UUID userId, long version) {
        UserReadModelEntity entity = new UserReadModelEntity();
        entity.setUserId(userId);
        entity.setUsername("alice");
        entity.setEmail("alice@test.local");
        entity.setStatus(UserStatus.ACTIVE);
        entity.setRole(UserRole.USER);
        entity.setVersion(version);
        entity.setUpdatedAt(OffsetDateTime.now());
        return entity;
    }
}
