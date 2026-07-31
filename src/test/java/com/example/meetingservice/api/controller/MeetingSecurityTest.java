package com.example.meetingservice.api.controller;

import com.example.meetingservice.api.dto.MeetingResponse;
import com.example.meetingservice.config.security.SecurityConfig;
import com.example.meetingservice.entity.MeetingStatus;
import com.example.meetingservice.service.MeetingQueryService;
import com.example.meetingservice.service.MeetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeetingController.class)
@Import(SecurityConfig.class)
class MeetingSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeetingService meetingService;

    @MockitoBean
    private MeetingQueryService meetingQueryService;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void rejectsMeetingApiRequestWithoutBearerToken() throws Exception {
        mockMvc.perform(get("/api/meetings/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsMeetingApiRequestWithJwtAccessToken() throws Exception {
        UUID meetingId = UUID.randomUUID();
        given(meetingQueryService.getById(meetingId)).willReturn(new MeetingResponse(
                meetingId,
                UUID.randomUUID(),
                "Planning",
                "Weekly planning",
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(1),
                MeetingStatus.SCHEDULED,
                List.of()
        ));

        mockMvc.perform(get("/api/meetings/{id}", meetingId).with(jwt()))
                .andExpect(status().isOk());
    }
}
