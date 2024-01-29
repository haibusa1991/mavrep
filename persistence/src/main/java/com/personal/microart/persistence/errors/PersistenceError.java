package com.personal.microart.persistence.errors;

public interface PersistenceError {

    Error getError();

    String getMessage();
}
