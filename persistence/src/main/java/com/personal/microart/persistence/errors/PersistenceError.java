package com.personal.microart.persistence.errors;

/**
 * Represents an error that occurred during a file system operation, e.g. read, write, delete, etc. Usually this is
 * an unrecoverable error and result in a 503 error being returned to the client.
 */
public interface PersistenceError {

    Error getError();

    String getMessage();
}
