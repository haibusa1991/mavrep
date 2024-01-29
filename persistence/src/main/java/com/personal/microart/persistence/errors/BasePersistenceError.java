package com.personal.microart.persistence.errors;

import lombok.Getter;

@Getter
public abstract class BasePersistenceError implements PersistenceError {
    private final Error error;
    private final String message;

    public BasePersistenceError(Error error, String message) {
        this.error = error;
        this.message = message;
    }
}
