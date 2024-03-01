package com.personal.microart.api.errors;

import lombok.Builder;

public class TokenInvalidError extends BaseApiError {

    @Builder
    public TokenInvalidError() {
        super(401, "Token is expired or already used.");
    }

    public static TokenInvalidError fromThrowable(Throwable throwable) {
        return TokenInvalidError.builder().build();
    }
}