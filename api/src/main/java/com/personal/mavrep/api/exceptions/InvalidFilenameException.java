package com.personal.mavrep.api.exceptions;

public class InvalidFilenameException extends MavrepException{

    public InvalidFilenameException() {
        super("Filename is not valid");
    }
}
