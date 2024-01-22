package com.personal.mavrep.api.errors;

import lombok.Builder;

public class ServiceUnavailableError extends BaseApiError {
    @Builder
    public ServiceUnavailableError() {
        super(503, "Service unavailable.");
    }
}
