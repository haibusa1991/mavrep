package com.personal.microart.persistence.errors;

import lombok.Builder;


public class ReadError extends BasePersistenceError {

    @Builder
    public ReadError(Error error, String message) {
        super(error, message);
    }
}
