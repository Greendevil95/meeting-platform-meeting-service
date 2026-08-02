package com.example.meetingservice.api.controller;

import com.example.meetingservice.api.dto.AddParticipantRequest;
import com.example.meetingservice.api.dto.CreateMeetingRequest;
import com.example.meetingservice.api.dto.MeetingResponse;
import com.example.meetingservice.api.dto.MeetingSummaryResponse;
import com.example.meetingservice.api.dto.UpdateMeetingRequest;
import com.example.meetingservice.service.MeetingQueryService;
import com.example.meetingservice.service.MeetingService;
import com.example.meetingservice.service.CurrentUserResolver;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/meetings")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingQueryService meetingQueryService;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse create(
            Authentication authentication,
            @Valid @RequestBody CreateMeetingRequest request
    ) {
        return meetingService.create(currentUserResolver.resolve(authentication).userId(), request);
    }

    @PutMapping("/{id}")
    public MeetingResponse update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMeetingRequest request
    ) {
        return meetingService.update(id, currentUserResolver.resolve(authentication), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(Authentication authentication, @PathVariable UUID id) {
        meetingService.cancel(id, currentUserResolver.resolve(authentication));
    }

    @PostMapping("/{id}/participants")
    public MeetingResponse addParticipant(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody AddParticipantRequest request
    ) {
        return meetingService.addParticipant(id, currentUserResolver.resolve(authentication), request);
    }

    @DeleteMapping("/{id}/participants/{userId}")
    public MeetingResponse removeParticipant(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        return meetingService.removeParticipant(id, userId, currentUserResolver.resolve(authentication));
    }

    @GetMapping
    public List<MeetingSummaryResponse> list(
            Authentication authentication,
            @RequestParam("from") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam("to") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return meetingQueryService.list(currentUserResolver.resolve(authentication), from, to);
    }

    @GetMapping("/{id}")
    public MeetingResponse getById(Authentication authentication, @PathVariable UUID id) {
        return meetingQueryService.getById(id, currentUserResolver.resolve(authentication));
    }
}
