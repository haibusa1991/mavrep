package com.personal.microart.api.errors;

import lombok.Builder;

public class FileNotFoundError extends BaseApiError {

    @Builder
    public FileNotFoundError() {
        super(404, "File not found");
    }
}
