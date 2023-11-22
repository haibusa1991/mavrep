package com.personal.mavrep.api.exceptions;

public class ArtefactRedeployException extends MavrepException{

    public ArtefactRedeployException() {
        super("Artefact version is already deployed");
    }
}
