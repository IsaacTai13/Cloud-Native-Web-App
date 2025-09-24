package com.isaactai.cloudnativeweb.common.error;

import java.time.Instant;

/**
 * Standardized API error response.
 *
 * <p>The {@link #of(int, String, String, String)} factory method is a convenience
 * for building a response with the current timestamp.
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(status, error, message, path, Instant.now());
    }
}
