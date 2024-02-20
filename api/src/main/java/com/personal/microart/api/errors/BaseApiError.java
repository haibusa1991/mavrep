package com.personal.microart.api.errors;

import lombok.Getter;

/**
 * Base implementation of the {@link ApiError} interface. All errors should extend this class.
 */
@Getter
public abstract class BaseApiError implements ApiError {
    private final Integer statusCode;
    private final String statusMessage;

    public BaseApiError(Integer statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }
}
