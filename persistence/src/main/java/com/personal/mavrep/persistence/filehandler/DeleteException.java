package com.personal.mavrep.persistence.filehandler;

import java.io.IOException;

public class DeleteException extends IOException {
    public DeleteException(String filename) {
        super(String.format("File '%s' cannot be deleted.", filename));
    }
}
