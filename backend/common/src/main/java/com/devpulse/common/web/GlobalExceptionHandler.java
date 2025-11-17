package com.devpulse.common.web;

import com.devpulse.common.dto.ApiResponse;
import com.devpulse.common.error.ApiError;
import com.devpulse.common.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler (Spring Boot 3 / Jakarta)
 *
 * Ensures all REST responses conform to ApiResponse<T>.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map((FieldError e) -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Validation failed: {}", details);

        ApiError error = ApiError.builder()
                .path(request.getRequestURI())
                .message("Validation failed")
                .detail(details)
                .build();

        ApiResponse<Object> resp = ApiResponse.<Object>builder()
                .success(false)
                .message("Validation error")
                .data(error)
                .build();

        return ResponseEntity.badRequest().body(resp);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadJson(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        log.warn("Malformed JSON request: {}", ex.getMessage());

        ApiError error = ApiError.builder()
                .path(request.getRequestURI())
                .message("Malformed JSON")
                .detail(ex.getMessage())
                .build();

        ApiResponse<Object> resp = ApiResponse.<Object>builder()
                .success(false)
                .message("Malformed JSON request")
                .data(error)
                .build();

        return ResponseEntity.badRequest().body(resp);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NotFoundException ex,
                                                              HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .path(request.getRequestURI())
                .message(ex.getMessage())
                .detail(null)
                .build();

        ApiResponse<Object> resp = ApiResponse.<Object>builder()
                .success(false)
                .message(ex.getMessage())
                .data(error)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAll(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: ", ex);

        ApiError error = ApiError.builder()
                .path(request.getRequestURI())
                .message("Internal server error")
                .detail(ex.getMessage())
                .build();

        ApiResponse<Object> resp = ApiResponse.<Object>builder()
                .success(false)
                .message("Internal server error")
                .data(error)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }
}
