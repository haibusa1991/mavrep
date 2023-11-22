package com.personal.mavrep.persistence.filehandler;

import java.io.IOException;

public class WriteException extends IOException {
    public WriteException(String filepath) {
        super(String.format("File cannot be written to directory '%s'.", filepath));
    }
}
