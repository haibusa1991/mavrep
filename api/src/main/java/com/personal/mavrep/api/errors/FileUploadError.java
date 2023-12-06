package com.personal.mavrep.api.errors;

import lombok.Builder;

@Builder
public class FileUploadError extends BaseApiError {

    public FileUploadError() {
        super(500,"Cannot upload file");
    }
}
