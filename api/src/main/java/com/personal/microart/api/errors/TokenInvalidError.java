package com.personal.microart.api.errors;

import lombok.Builder;
/**
 * API error that indicates that a password recovery token is used or expired.
 * Error contains HTTP status code to 401 (Unauthorized) and a "Token is expired or already used." message.
 */
public class TokenInvalidError extends BaseApiError {

    @Builder
    public TokenInvalidError() {
        super(401, "Token is expired or already used.");
    }

    public static TokenInvalidError fromThrowable(Throwable throwable) {
        return TokenInvalidError.builder().build();
    }
}