package com.personal.mavrep.api.errors;

public class FileNotFoundError extends BaseApiError {

    public FileNotFoundError() {
        super(404, "File not found");
    }
}
