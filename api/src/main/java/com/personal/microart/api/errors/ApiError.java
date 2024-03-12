package com.personal.microart.api.errors;

/**
 * Represents an incomplete or failed operation. All processors return an ApiError if the operation fails for some reason.
 * It provides getters for the HTTP status code and the status message of the error.
 */
public interface ApiError {

    /**
     * HTTP status code of the error.
     *
     * @return Integer representing the status code of the error.
     */
    Integer getStatusCode();

    /**
     * Status message of the error.
     *
     * @return String representing the status message of the error.
     */
    String getStatusMessage();
}