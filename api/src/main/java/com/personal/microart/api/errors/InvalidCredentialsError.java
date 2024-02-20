package com.personal.microart.api.errors;

import lombok.Builder;

public class InvalidCredentialsError extends BaseApiError {
    @Builder
    public InvalidCredentialsError() {
        super(403, "Email or password is incorrect.");
    }
}
