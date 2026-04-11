package com.example.meetingservice.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiServiceException {
    private static final String CODE = "error.conflict";

    public ConflictException(String message) {
        super(CODE, message, HttpStatus.CONFLICT);
    }

    public ConflictException(String code, String message) {
        super(code, message, HttpStatus.CONFLICT);
    }
}
