package com.example.meetingservice.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiServiceException {
    private static final String CODE = "error.entity.not.found";

    public NotFoundException(String message) {
        super(CODE, message, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String code, String message) {
        super(code, message, HttpStatus.NOT_FOUND);
    }
}
