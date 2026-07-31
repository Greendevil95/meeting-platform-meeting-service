package com.example.meetingservice.kafka.user;

import com.example.meetingservice.config.KafkaTopicsProperties;
import com.example.meetingservice.entity.UserStatus;
import com.example.meetingservice.service.UserProfile;
import com.example.meetingservice.service.UserReadModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Slf4j
@Component
public class UserEventsConsumer {
    private final UserReadModelService userReadModelService;
    private final JsonMapper jsonMapper;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @KafkaListener(topics = "${meeting.kafka.topics.user-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void onUserCreated(String payload) {
        consume(kafkaTopicsProperties.getUserCreated(), payload, () -> {
            UserCreatedEvent event = jsonMapper.readValue(payload, UserCreatedEvent.class);
            userReadModelService.upsertUser(new UserProfile(
                    event.userId(),
                    event.keycloakSubject(),
                    event.username(),
                    event.email(),
                    event.status(),
                    event.role(),
                    event.version(),
                    event.timestamp()
            ));
        });
    }

    @KafkaListener(topics = "${meeting.kafka.topics.user-updated}", groupId = "${spring.kafka.consumer.group-id}")
    public void onUserUpdated(String payload) {
        consume(kafkaTopicsProperties.getUserUpdated(), payload, () -> {
            UserUpdatedEvent event = jsonMapper.readValue(payload, UserUpdatedEvent.class);
            userReadModelService.upsertUser(new UserProfile(
                    event.userId(),
                    event.keycloakSubject(),
                    event.username(),
                    event.email(),
                    event.status(),
                    event.role(),
                    event.version(),
                    event.timestamp()
            ));
        });
    }

    @KafkaListener(topics = "${meeting.kafka.topics.user-deleted}", groupId = "${spring.kafka.consumer.group-id}")
    public void onUserDeleted(String payload) {
        consume(kafkaTopicsProperties.getUserDeleted(), payload, () -> {
            UserDeletedEvent event = jsonMapper.readValue(payload, UserDeletedEvent.class);
            userReadModelService.updateStatus(event.userId(), UserStatus.DELETED, event.version(), event.timestamp());
        });
    }

    @KafkaListener(topics = "${meeting.kafka.topics.user-status-changed}", groupId = "${spring.kafka.consumer.group-id}")
    public void onUserStatusChanged(String payload) {
        consume(kafkaTopicsProperties.getUserStatusChanged(), payload, () -> {
            UserStatusChangedEvent event = jsonMapper.readValue(payload, UserStatusChangedEvent.class);
            userReadModelService.updateStatus(event.userId(), event.status(), event.version(), event.timestamp());
        });
    }

    private void consume(String topic, String payload, ThrowingRunnable action) {
        try {
            action.run();
        } catch (JacksonException | IllegalArgumentException ex) {
            log.error("Failed to process non-retryable {} event: {}", topic, payload, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to process {} event: {}", topic, payload, ex);
            throw new IllegalStateException("Cannot process user event", ex);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
