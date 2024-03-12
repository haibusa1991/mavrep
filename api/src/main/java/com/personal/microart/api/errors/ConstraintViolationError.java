package com.personal.microart.api.errors;

import lombok.Builder;
/**
 * API error that occurs when an input constraint is violated. (e.g. input field is too long, too short, etc.)
 * Error contains HTTP status code to 400 (Bad Request) and a list of violated constraints.
 */
public class ConstraintViolationError extends BaseApiError {

    @Builder
    public ConstraintViolationError(String statusMessage) {
        super(400, statusMessage);
    }
}