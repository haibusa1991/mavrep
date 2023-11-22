package com.personal.mavrep.api.exceptions;

public class FileNotFoundException extends MavrepException{

    public FileNotFoundException() {
        super("File not found");
    }
}
