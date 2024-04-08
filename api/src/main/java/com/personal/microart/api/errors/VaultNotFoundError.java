package com.personal.microart.api.errors;

import lombok.Builder;

/**
 * API error that indicates that a vault with the specified name does not exist.
 * Provides a static factory method to create an instance of the error.
 * Error contains HTTP status code to 404 and a message "Vault does not exist."
 */
public class VaultNotFoundError extends BaseApiError {
    @Builder
    public VaultNotFoundError() {
        super(404, "Vault does not exist.");
    }

    public static VaultNotFoundError fromThrowable(Throwable throwable) {
        return VaultNotFoundError.builder().build();
    }
}
