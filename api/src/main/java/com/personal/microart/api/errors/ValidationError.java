package com.personal.microart.api.errors;

import lombok.Builder;

public class ValidationError extends BaseApiError {
    @Builder
    public ValidationError(String message) {
        super(400, message);
    }
}
