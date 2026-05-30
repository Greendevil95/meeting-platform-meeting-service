package com.example.meetingservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.meetingservice.api.dto.MeetingParticipantRequest;
import com.example.meetingservice.api.dto.MeetingResponse;
import com.example.meetingservice.entity.MeetingStatus;
import com.example.meetingservice.entity.ParticipantRole;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class CacheConfigTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void meetingCacheDeserializesValuesAsMeetingResponse() {
        var cacheConfiguration = CacheConfig.meetingCacheConfiguration(jsonMapper, Duration.ofMinutes(30));
        MeetingResponse response = new MeetingResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Planning",
                "Sprint planning",
                OffsetDateTime.parse("2026-05-30T10:00:00Z"),
                OffsetDateTime.parse("2026-05-30T11:00:00Z"),
                MeetingStatus.SCHEDULED,
                List.of(new MeetingParticipantRequest(UUID.randomUUID(), ParticipantRole.ATTENDEE))
        );

        var bytes = cacheConfiguration.getValueSerializationPair().write(response);
        Object restored = cacheConfiguration.getValueSerializationPair().read(bytes);

        assertThat(restored).isEqualTo(response);
    }
}
