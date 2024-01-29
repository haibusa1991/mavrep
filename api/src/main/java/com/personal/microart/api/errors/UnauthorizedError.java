package com.personal.microart.api.errors;

import lombok.Builder;

public class UnauthorizedError extends BaseApiError {
    @Builder
    public UnauthorizedError() {
        super(403, "Unauthorized");
    }
}
