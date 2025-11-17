package com.devpulse.common.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standardized error payload used inside ApiResponse.data for errors when needed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {
    private Instant timestamp = Instant.now();
    private String path;
    private String message;
    private String detail;
}
