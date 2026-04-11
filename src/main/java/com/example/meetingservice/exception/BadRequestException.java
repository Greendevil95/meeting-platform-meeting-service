package com.example.meetingservice.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiServiceException {
    private static final String CODE = "error.invalid.request";

    public BadRequestException(String message) {
        super(CODE, message, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String code, String message) {
        super(code, message, HttpStatus.BAD_REQUEST);
    }
}
