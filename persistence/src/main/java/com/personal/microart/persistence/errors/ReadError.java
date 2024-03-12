package com.personal.microart.persistence.errors;

import lombok.Builder;
/**
 * Persistence error indicating that a file was cannot be read from the file system for some reason,
 * e.g. it does not exist, insufficient permissions, locked, etc.
 * Error type is {@link Error#READ_ERROR} and a custom message.
 */

public class ReadError extends BasePersistenceError {

    @Builder
    public ReadError(Error error, String message) {
        super(error, message);
    }
}
