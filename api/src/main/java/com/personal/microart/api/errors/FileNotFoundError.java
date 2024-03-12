package com.personal.microart.api.errors;

import lombok.Builder;
/**
 * API error that a file is not read from the file system for some reason, e.g. it does not exist or user does not have permission to read it.
 * Error contains HTTP status code to 404 (Not found) and a "File not found" message.
 */
public class FileNotFoundError extends BaseApiError {

    @Builder
    public FileNotFoundError() {
        super(404, "File not found");
    }
}
