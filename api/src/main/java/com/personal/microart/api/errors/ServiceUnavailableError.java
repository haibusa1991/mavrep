package com.personal.microart.api.errors;

import lombok.Builder;

public class ServiceUnavailableError extends BaseApiError {
    @Builder
    public ServiceUnavailableError() {
        super(503, "Service unavailable.");
    }

    public static ServiceUnavailableError fromThrowable(Throwable throwable) {
        return ServiceUnavailableError.builder().build();
    }
}
