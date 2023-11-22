package com.personal.mavrep.persistence.filehandler;

import java.io.IOException;

public class ReadException extends IOException {
    public ReadException(String pathAndFilename) {
        super(String.format("File %s cannot be read", pathAndFilename));
    }
}
