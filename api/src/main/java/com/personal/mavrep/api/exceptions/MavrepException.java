package com.personal.mavrep.api.exceptions;

public abstract class MavrepException extends RuntimeException{

    public MavrepException(String message) {
        super(message);
    }
}
