package com.personal.microart.persistence.errors;

import lombok.Builder;
/**
 * Persistence error indicating that a file was cannot be deleted from the file system for some reason,
 * e.g. it does not exist, insufficient permissions, locked, etc.
 * Error type is {@link Error#FILE_DELETE_ERROR} and message is "Unable to delete file".
 */

public class DeleteError extends BasePersistenceError {

    @Builder
    public DeleteError() {
        super(Error.FILE_DELETE_ERROR, "Unable to delete file");
    }
}
