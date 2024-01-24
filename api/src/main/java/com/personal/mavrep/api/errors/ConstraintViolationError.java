package com.personal.mavrep.api.errors;

import lombok.Builder;

public class ConstraintViolationError extends BaseApiError {

    @Builder
    public ConstraintViolationError(String statusMessage) {
        super(400, statusMessage);
    }
}