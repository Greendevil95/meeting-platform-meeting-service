package com.example.meetingservice.service;

import com.example.meetingservice.config.KafkaTopicsProperties;
import com.example.meetingservice.entity.OutboxEventEntity;
import com.example.meetingservice.kafka.meeting.EventType;
import com.example.meetingservice.kafka.meeting.MeetingCreatedEvent;
import com.example.meetingservice.metrics.MeetingMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxProcessingService outboxProcessingService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private MeetingMetrics meetingMetrics;

    private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void recordsPublishedKafkaEventAfterSuccessfulSend() {
        KafkaTopicsProperties topicsProperties = new KafkaTopicsProperties();
        topicsProperties.setMeetingCreated("meeting-service.meeting-created");
        OutboxPublisher publisher = new OutboxPublisher(
                outboxProcessingService,
                topicsProperties,
                kafkaTemplate,
                jsonMapper,
                meetingMetrics
        );
        UUID outboxEventId = UUID.randomUUID();
        UUID processingToken = UUID.randomUUID();
        OutboxEventEntity outboxEvent = new OutboxEventEntity();
        outboxEvent.setId(outboxEventId);
        outboxEvent.setProcessingToken(processingToken);
        outboxEvent.setAggregateId(UUID.randomUUID().toString());
        outboxEvent.setEventType(EventType.MEETING_CREATED);
        outboxEvent.setEventJson(jsonMapper.convertValue(
                new MeetingCreatedEvent(
                        UUID.randomUUID(),
                        OffsetDateTime.now(),
                        1L,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Planning",
                        "Weekly planning",
                        OffsetDateTime.now().plusDays(1),
                        OffsetDateTime.now().plusDays(1).plusHours(1)
                ),
                new TypeReference<>() {}
        ));
        when(outboxProcessingService.claimPendingBatch()).thenReturn(List.of(outboxEvent));
        when(kafkaTemplate.send(eq("meeting-service.meeting-created"), eq(outboxEvent.getAggregateId()), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publishPending();

        verify(outboxProcessingService).markPublished(outboxEventId, processingToken);
        verify(meetingMetrics).recordKafkaEventPublished(EventType.MEETING_CREATED, "meeting-service.meeting-created");
    }
}
