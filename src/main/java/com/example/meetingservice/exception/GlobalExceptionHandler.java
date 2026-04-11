package com.example.meetingservice.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final String serviceName;

    public GlobalExceptionHandler(@Value("${spring.application.name}") String serviceName) {
        this.serviceName = serviceName;
    }

    @ExceptionHandler(ApiServiceException.class)
    public ResponseEntity<ApiError> handleApiServiceException(ApiServiceException ex) {
        ApiError error = error(ex.getCode(), ex.getMessage());
        log.error("ApiServiceException: code={}, message={}", ex.getCode(), ex.getMessage(), ex);
        return ResponseEntity.status(ex.getHttpStatus()).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        ApiError error = error("error.internal", "Internal server error: " + ex.getMessage());
        log.error("Unexpected error", ex);
        return ResponseEntity.status(500).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest().body(error("error.validation", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJsonParse(HttpMessageNotReadableException ex) {
        log.warn("Invalid JSON request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(error("error.json.invalid", "Invalid JSON: " + ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        String errors = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("Constraint violation: {}", errors);
        return ResponseEntity.badRequest().body(error("error.constraint", errors));
    }

    public record ApiError(String code, String message, String serviceName) {}

    private ApiError error(String code, String message) {
        return new ApiError(code, message, serviceName);
    }
}
