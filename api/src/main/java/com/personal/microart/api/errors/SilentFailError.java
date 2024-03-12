package com.personal.microart.api.errors;

import lombok.Builder;

/**
 * This a special type of API error that fails silently. Even if it is a failure, it does not return an error message.
 * Used when the client should not be informed of the failure, e.g. trying to reset a password and provided email is
 * not found in the database.
 * Provides a static factory method to create an instance of the error.
 * Error contains HTTP status code to 204 (No Content) and an empty error message.
 */
public class SilentFailError extends BaseApiError {
    @Builder
    public SilentFailError() {
        super(204, "");
    }

    public static SilentFailError fromThrowable(Throwable throwable) {
        return SilentFailError.builder().build();
    }
}
