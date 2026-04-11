package com.example.meetingservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiServiceException extends RuntimeException {

    private final String code;
    private final HttpStatus httpStatus;

    protected ApiServiceException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

}
