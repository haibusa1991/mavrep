package com.personal.microart.api.errors;

import lombok.Builder;
//TODO: Delete this class
public class UnauthorizedError extends BaseApiError {
    @Builder
    public UnauthorizedError() {
        super(403, "Unauthorized");
    }
}
