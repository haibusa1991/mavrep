package com.personal.mavrep.api.errors;

import lombok.Getter;

@Getter
public abstract class BaseApiError implements ApiError {
    private final Integer statusCode;
    private final String statusMessage;

    public BaseApiError(Integer statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }
}
