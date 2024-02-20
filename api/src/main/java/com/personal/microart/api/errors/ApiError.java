package com.personal.microart.api.errors;

/**
 * This interface represents an API error.
 * It provides methods to retrieve the status code and status message of an error. All errors which are
 * returned by the processors should implement this interface.
 */
public interface ApiError {

    /**
     * Retrieves the HTTP status code of the error.
     *
     * @return Integer representing the status code of the error.
     */
    Integer getStatusCode();

    /**
     * Retrieves the status message of the error.
     *
     * @return String representing the status message of the error.
     */
    String getStatusMessage();
}