package com.personal.microart.api.errors;

import lombok.Builder;

/**
 * API error that indicates that the a vault with the same name already exists.
 * Provides a static factory method to create an instance of the error.
 * Error contains HTTP status code to 409 and a message "Duplicate vault name."
 */
public class DuplicateVaultNameError extends BaseApiError {
    @Builder
    public DuplicateVaultNameError() {
        super(409, "Duplicate vault name.");
    }

    public static DuplicateVaultNameError fromThrowable(Throwable throwable) {
        return DuplicateVaultNameError.builder().build();
    }
}
