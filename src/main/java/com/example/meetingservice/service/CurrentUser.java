package com.example.meetingservice.service;

import java.util.UUID;

public record CurrentUser(UUID userId, boolean admin) {
}
