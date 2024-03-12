package com.personal.microart.persistence.errors;

import lombok.Builder;
/**
 * Persistence error indicating that a file was cannot be written to the file system for some reason,
 * e.g. insufficient permissions, locked, etc.
 * Error type is {@link Error#WRITE_ERROR} and a custom message.
 */

public class WriteError extends BasePersistenceError {
    @Builder
    public WriteError(String message) {
        super(Error.WRITE_ERROR, message);
    }
}
