package com.personal.microart.api.errors;

import lombok.Builder;
/**
 * Generic error that something went wrong with the service. Hides all 500 errors.
 * Provides a static factory method to create an instance of the error.
 * Error contains HTTP status code to 503 (Service unavailable) and a "Service unavailable" message.
 */
public class ServiceUnavailableError extends BaseApiError {
    @Builder
    public ServiceUnavailableError() {
        super(503, "Service unavailable.");
    }

    public static ServiceUnavailableError fromThrowable(Throwable throwable) {
        return ServiceUnavailableError.builder().build();
    }
}
