package com.personal.microart.api.errors;

import lombok.Builder;

/**
 * API error that indicates that the current user does not have permission to perform the requested operation.
 * Provides a static factory method to create an instance of the error.
 * Error contains HTTP status code to 403 (Forbidden) and an "Unauthorized." message.
 */
public class UnauthorizedError extends BaseApiError {
    @Builder
    public UnauthorizedError() {
        super(403, "Unauthorized.");
    }

    public static UnauthorizedError fromThrowable(Throwable throwable) {
        return UnauthorizedError.builder().build();
    }
}
