package com.personal.microart.api.errors;

import lombok.Builder;

/**
 * API error that user credentials are invalid, e.g. email or password is incorrect, jwt expired, account is locked, etc.
 * Provides a static factory method to create an instance of the error.
 * Error contains HTTP status code to 403 (Forbidden) and an "Email or password is incorrect." message.
 */
public class InvalidCredentialsError extends BaseApiError {
    @Builder
    public InvalidCredentialsError() {
        super(403, "Email or password is incorrect.");
    }

    public static InvalidCredentialsError fromThrowable(Throwable throwable) {
        return InvalidCredentialsError.builder().build();
    }
}
