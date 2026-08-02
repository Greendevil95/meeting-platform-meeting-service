package com.example.meetingservice.api.controller;

import com.example.meetingservice.api.dto.MeetingResponse;
import com.example.meetingservice.config.security.SecurityConfig;
import com.example.meetingservice.entity.MeetingStatus;
import com.example.meetingservice.service.MeetingQueryService;
import com.example.meetingservice.service.MeetingService;
import com.example.meetingservice.service.CurrentUserResolver;
import com.example.meetingservice.service.CurrentUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
    private CurrentUserResolver currentUserResolver;

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
        given(currentUserResolver.resolve(any())).willReturn(new CurrentUser(UUID.randomUUID(), false));
        given(meetingQueryService.getById(eq(meetingId), any())).willReturn(new MeetingResponse(
                meetingId,
                UUID.randomUUID(),
                "Planning",
                "Weekly planning",
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(1),
                MeetingStatus.SCHEDULED,
                List.of()
        ));

        mockMvc.perform(get("/api/meetings/{id}", meetingId).with(userJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsJwtWithoutApplicationRole() throws Exception {
        mockMvc.perform(get("/api/meetings/{id}", UUID.randomUUID()).with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void derivesMeetingOrganizerFromJwtInsteadOfRequestBody() throws Exception {
        UUID authenticatedUserId = UUID.randomUUID();
        given(currentUserResolver.resolve(any())).willReturn(new CurrentUser(authenticatedUserId, false));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/meetings")
                        .with(userJwt())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "organizerUserId": "00000000-0000-0000-0000-000000000001",
                                  "title": "Planning",
                                  "description": "Weekly planning",
                                  "startAt": "2030-01-01T10:00:00Z",
                                  "endAt": "2030-01-01T11:00:00Z",
                                  "participantUserIds": ["00000000-0000-0000-0000-000000000002"]
                                }
                                """))
                .andExpect(status().isCreated());

        verify(meetingService).create(eq(authenticatedUserId), any());
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor userJwt() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
