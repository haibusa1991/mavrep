package com.personal.mavrep.persistence.errors;

public interface PersistenceError {

    Error getError();

    String getMessage();
}
