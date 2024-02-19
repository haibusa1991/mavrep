package com.personal.microart.core.processor.browse;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.operations.browse.BrowseInput;
import com.personal.microart.api.operations.browse.BrowseOperation;
import com.personal.microart.api.operations.browse.BrowseResult;
import com.personal.microart.api.operations.browse.Content;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.repositories.VaultRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BrowseCore implements BrowseOperation {
    private final VaultRepository vaultRepository;

    @Override
    public Either<ApiError, BrowseResult> process(BrowseInput input) {
        return Try.of(() -> BrowseResult
                        .builder()
                        .content(this.getFindableArtefacts()
                                .stream()
                                .filter(artefact -> artefact.getUri().startsWith(this.transformBrowseToMvn(input.getUri())))
                                .map(artefact -> this.getContent(artefact, input.getUri()))
                                .sorted()
                                .collect(Collectors.toCollection(LinkedHashSet::new)))
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

    private Set<Artefact> getFindableArtefacts() {

        Object authDetails = SecurityContextHolder.getContext().getAuthentication().getDetails();

        Set<Vault> vaults = authDetails instanceof MicroartUser
                ? this.vaultRepository.findAllByAuthorizedUsersContainingOrIsPublicTrue((MicroartUser) authDetails)
                : this.vaultRepository.findAllByIsPublicTrue();

        return vaults
                .stream()
                .map(Vault::getArtefacts)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }
}
