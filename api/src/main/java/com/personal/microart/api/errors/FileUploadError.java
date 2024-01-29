package com.personal.microart.api.errors;

import lombok.Builder;

public class FileUploadError extends BaseApiError {

    @Builder
    public FileUploadError(String message) {
        super(500, message);
    }

    @Builder
    public FileUploadError() {
        super(500, "Cannot upload file");
    }
}
