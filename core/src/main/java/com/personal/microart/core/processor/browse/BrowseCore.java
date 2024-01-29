package com.personal.microart.core.processor.browse;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.operations.browse.BrowseInput;
import com.personal.microart.api.operations.browse.BrowseOperation;
import com.personal.microart.api.operations.browse.BrowseResult;
import com.personal.microart.api.operations.browse.Content;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BrowseCore implements BrowseOperation {
    private final ArtefactRepository artefactRepository;

    @Override
    public Either<ApiError, BrowseResult> process(BrowseInput input) {
        return Try.of(() -> BrowseResult
                        .builder()
                        .content(this.artefactRepository
                                .findArtefactByUriStartingWith(this.transformBrowseToMvn(input.getUri())) // TODO: should include filter for current user
                                .stream()
                                .map(artefact -> this.getContent(artefact, input.getUri()))
                                .collect(Collectors.toSet()))
                        .build())
                .toEither()
                .mapLeft(throwable -> ServiceUnavailableError.builder().build());
    }

    private Content getContent(Artefact artefact, String uri) {
        String remainingUri = artefact
                .getUri()
                .substring(this.transformBrowseToMvn(uri).length());

        String[] remainingUriElements = Arrays
                .stream(remainingUri.split("/"))
                .filter(element -> !element.isBlank())
                .toArray(String[]::new);

        if (remainingUriElements.length > 1) {
            String nextElement = remainingUriElements[0];
            String finalUri = uri + "/" + nextElement;

            return Content
                    .builder()
                    .name(remainingUriElements[0])
                    .uri(finalUri)
                    .build();
        }

        return Content
                .builder()
                .name(remainingUriElements[0])
                .uri(this.transformBrowseToMvn(uri + "/" + remainingUriElements[0]))
                .build();
    }

    private String transformBrowseToMvn(String browseUri) {
        String[] uriElements = Arrays.stream(browseUri.split("/"))
                .filter(element -> !element.isBlank())
                .toArray(String[]::new);

        uriElements[0] = "mvn";

        return "/" + String.join("/", uriElements);
    }
}
