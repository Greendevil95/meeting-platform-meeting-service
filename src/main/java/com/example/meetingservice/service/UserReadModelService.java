package com.example.meetingservice.service;

import com.example.meetingservice.entity.UserRole;
import com.example.meetingservice.entity.UserStatus;
import com.example.meetingservice.entity.UserReadModelEntity;
import com.example.meetingservice.repository.UserReadModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserReadModelService {

    private final UserReadModelRepository repository;
    private final UserReadModelMapper userReadModelMapper;

    public UserReadModelService(
            UserReadModelRepository repository,
            UserReadModelMapper userReadModelMapper
    ) {
        this.repository = repository;
        this.userReadModelMapper = userReadModelMapper;
    }

    @Transactional
    public UserProfile upsertUser(UserProfile profile) {
        repository.findById(profile.userId())
                .ifPresentOrElse(existing -> applyIfNewer(existing, profile), () -> create(profile));
        return profile;
    }

    @Transactional
    public void updateStatus(UUID userId, UserStatus status, long version, OffsetDateTime eventTime) {
        UserReadModelEntity entity = repository.findById(userId).orElseGet(() -> {
            UserReadModelEntity created = new UserReadModelEntity();
            created.setUserId(userId);
            created.setUsername("unknown");
            created.setEmail("unknown");
            created.setRole(UserRole.USER);
            created.setStatus(status);
            created.setVersion(version);
            created.setUpdatedAt(eventTime);
            return created;
        });
        if (version <= entity.getVersion()) {
            return;
        }
        entity.setStatus(status);
        entity.setVersion(version);
        entity.setUpdatedAt(eventTime);
        repository.save(entity);
        userReadModelMapper.toProfile(entity);
    }

    @Transactional(readOnly = true)
    public Optional<UserProfile> findUser(UUID userId) {
        return repository.findById(userId).map(userReadModelMapper::toProfile);
    }

    private void create(UserProfile profile) {
        UserReadModelEntity entity = userReadModelMapper.toEntity(profile);
        repository.save(entity);
    }

    private void applyIfNewer(UserReadModelEntity existing, UserProfile profile) {
        if (profile.version() <= existing.getVersion()) {
            return;
        }
        userReadModelMapper.updateEntity(profile, existing);
        repository.save(existing);
    }
}
