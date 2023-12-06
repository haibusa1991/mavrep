package com.personal.mavrep.api.errors;

import lombok.Builder;

public class InvalidFileExtensionError extends BaseApiError {
    @Builder
    public InvalidFileExtensionError() {
        super(400, "File extension is not valid");
    }
}
