package com.personal.microart.api.errors;

import lombok.Builder;
/**
 * API error that a file cannot be written to the file system for some reason, e.g. write permission denied, disk full, etc.
 * Error contains HTTP status code to 503 (Not found) and a "Cannot upload file" message.
 */
public class FileUploadError extends BaseApiError {

    @Builder
    public FileUploadError(String message) {
        super(503, message);
    }

    @Builder
    public FileUploadError() {
        super(503, "Cannot upload file");
    }
}