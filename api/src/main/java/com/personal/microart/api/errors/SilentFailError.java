package com.personal.microart.api.errors;

import lombok.Builder;

/**
 * This a special type of API error that fails silently. Even if it is a failure, it does not return an error message.
 * Used specifically for cases where the error is not critical and the user does not need to be informed about it.
 */
public class SilentFailError extends BaseApiError {
    @Builder
    public SilentFailError() {
        super(204, "");
    }

    public static SilentFailError fromThrowable(Throwable throwable) {
        return SilentFailError.builder().build();
    }
}
