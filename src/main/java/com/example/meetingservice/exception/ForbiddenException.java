package com.example.meetingservice.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiServiceException {
    private static final String CODE = "error.forbidden";

    public ForbiddenException(String message) {
        super(CODE, message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String code, String message) {
        super(code, message, HttpStatus.FORBIDDEN);
    }
}
