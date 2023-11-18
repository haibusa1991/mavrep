package com.personal.mavrep.rest.exceptions;

public class VersionAlreadyDeployedException extends RuntimeException {
    public VersionAlreadyDeployedException(String artefactId) {
        super(String.format("Artefact '%s' already deployed.", artefactId));

    }
}
