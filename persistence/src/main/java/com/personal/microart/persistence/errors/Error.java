package com.personal.microart.persistence.errors;

/**
 * Specifies the type of error that occurred during a file system operation, e.g. read, write, delete, etc.
 */
public enum Error {
    WRITE_ERROR,
    READ_ERROR,
    FILE_NOT_FOUND_ERROR,
    DATABASE_ERROR,
    FILE_DELETE_ERROR
}
