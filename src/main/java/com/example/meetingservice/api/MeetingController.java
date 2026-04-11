package com.example.meetingservice.api;

import com.example.meetingservice.api.dto.AddParticipantRequest;
import com.example.meetingservice.api.dto.CancelMeetingRequest;
import com.example.meetingservice.api.dto.CreateMeetingRequest;
import com.example.meetingservice.api.dto.MeetingResponse;
import com.example.meetingservice.api.dto.MeetingSummaryResponse;
import com.example.meetingservice.api.dto.UpdateMeetingRequest;
import com.example.meetingservice.service.MeetingQueryService;
import com.example.meetingservice.service.MeetingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/meetings")
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingQueryService meetingQueryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse create(@Valid @RequestBody CreateMeetingRequest request) {
        return meetingService.create(request);
    }

    @PutMapping("/{id}")
    public MeetingResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateMeetingRequest request) {
        return meetingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID id, @Valid @RequestBody CancelMeetingRequest request) {
        meetingService.cancel(id, request);
    }

    @PostMapping("/{id}/participants")
    public MeetingResponse addParticipant(@PathVariable UUID id, @Valid @RequestBody AddParticipantRequest request) {
        return meetingService.addParticipant(id, request);
    }

    @DeleteMapping("/{id}/participants/{userId}")
    public MeetingResponse removeParticipant(
            @PathVariable UUID id,
            @PathVariable Long userId,
            @RequestParam("requestorId") @NotNull Long requestorId
    ) {
        return meetingService.removeParticipant(id, userId, requestorId);
    }

    @GetMapping
    public List<MeetingSummaryResponse> list(
            @RequestParam("userId") @NotNull Long userId,
            @RequestParam("from") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam("to") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return meetingQueryService.list(userId, from, to);
    }

    @GetMapping("/{id}")
    public MeetingResponse getById(@PathVariable UUID id) {
        return meetingQueryService.getById(id);
    }
}
