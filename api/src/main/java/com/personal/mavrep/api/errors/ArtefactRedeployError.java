package com.personal.mavrep.api.errors;

import lombok.Builder;

@Builder
public class ArtefactRedeployError extends BaseApiError {

    public ArtefactRedeployError() {
        super(409,"Artefact version is already deployed");
    }
}
