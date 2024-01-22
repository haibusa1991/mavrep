package com.personal.mavrep.persistence.errors;

import lombok.Builder;


public class DeleteError extends BasePersistenceError {

    @Builder
    public DeleteError() {
        super(Error.FILE_DELETE_ERROR, "Unable to delete file");
    }
}
