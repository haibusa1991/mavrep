package com.personal.mavrep.persistence.errors;

import lombok.Builder;


public class WriteError extends BasePersistenceError {
    @Builder
    public WriteError(String message) {
        super(Error.WRITE_ERROR, message);
    }
}
