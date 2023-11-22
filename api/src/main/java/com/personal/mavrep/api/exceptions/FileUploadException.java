package com.personal.mavrep.api.exceptions;

public class FileUploadException extends MavrepException{

    public FileUploadException() {
        super("Cannot upload file");
    }
}
