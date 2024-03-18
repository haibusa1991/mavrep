package com.personal.microart.api.errors;

import lombok.Builder;

/**
 * API error that indicates that the requested user does not exist.
 * Provides a static factory method to create an instance of the error.
 * Error contains HTTP status code to 404 (Not Found) and a custom message.
 */
public class NotFoundError extends BaseApiError {
    @Builder
    public NotFoundError(String message) {
        super(404, message);
    }

    public static NotFoundError fromThrowable(Throwable throwable) {
        return NotFoundError
                .builder()
                .message(throwable.getMessage())
                .build();
    }
}
