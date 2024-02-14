package com.personal.microart.core.processor.file;

import com.personal.microart.api.errors.*;
import com.personal.microart.api.operations.file.upload.UploadFileInput;
import com.personal.microart.api.operations.file.upload.UploadFileOperation;
import com.personal.microart.api.operations.file.upload.UploadFileResult;
import com.personal.microart.core.auth.basic.BasicAuth;
import com.personal.microart.core.processor.UriProcessor;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.filehandler.FileWriter;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import io.vavr.API;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.hibernate.JDBCException;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@RequiredArgsConstructor
@Component
@Transactional
public class UploadFileCore implements UploadFileOperation {
    private final FileWriter fileWriter;
    private final ArtefactRepository artefactRepository;
    private final VaultRepository vaultRepository;
    private final UriProcessor uriProcessor;
    private final ConversionService conversionService;


    @Override
    public Either<ApiError, UploadFileResult> process(UploadFileInput input) {

        return this.validatePermissions(input)
                .flatMap(this::validateFilename)
                .flatMap(this::createFileRecord)
                .flatMap(this::writeFile)
                .flatMap(this::updateFilename);

    }

    private Either<ApiError, UploadFileInput> validatePermissions(UploadFileInput input) {
        String vaultName = this.uriProcessor.getVaultName(input.getUri());

        return Try.of(() -> {

                    MicroartUser user = (MicroartUser) SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            .getDetails();

                    Vault vault = this.vaultRepository
                            .findVaultByName(vaultName)
                            .orElseGet(() -> this.canCreateVault(input)
                                    ? this.vaultRepository.save(Vault.builder().name(vaultName).user(user).build())
                                    : Vault.builder().name(UUID.randomUUID().toString()).build());

                    if (!canUpdateVault(vault)) {
                        throw new IllegalArgumentException();
                    }

                    return Stream.of(vault)
                            .map(Vault::getAuthorizedUsers)
                            .map(authorizedUsers -> authorizedUsers.contains(user))
                            .map(ignored -> input)
                            .findFirst()
                            .orElseThrow(IllegalArgumentException::new);
                })
                .toEither()
                .mapLeft(throwable -> API.Match(throwable).of(
                        Case($(instanceOf(JDBCException.class)), exception -> ServiceUnavailableError.builder().build()),
                        Case($(instanceOf(IllegalArgumentException.class)), exception -> UnauthorizedError.builder().build())
                ));
    }

    private Either<ApiError, UploadFileInput> validateFilename(UploadFileInput input) {
        return Try.of(() -> {
                    String[] uriElements = input.getUri().split("/");
                    String filename = uriElements[uriElements.length - 1].toLowerCase();

                    Pattern typical = Pattern.compile("\\d{8}\\.\\d{6}-1\\..*");
                    Pattern javadoc = Pattern.compile("\\d{8}\\.\\d{6}-1-javadoc\\..*");
                    Pattern sources = Pattern.compile("\\d{8}\\.\\d{6}-1-sources\\..*");
                    Pattern metadata = Pattern.compile("maven-metadata\\.xml.*");

                    return Stream.of(typical, javadoc, sources, metadata)
                            .map(regex -> regex.matcher(filename.toLowerCase()))
                            .map(matcher -> matcher.find() ? matcher.group() : "")
                            .filter(result -> !result.isEmpty())
                            .findFirst()
                            .map(ignored -> input)
                            .orElseThrow(() -> new IllegalArgumentException("Version already deployed or filename is invalid."));

                })
                .toEither()
                .mapLeft(throwable -> ValidationError.builder().message(throwable.getMessage()).build());
    }

    private Either<ApiError, UploadFileInput> createFileRecord(UploadFileInput input) {
        String vaultName = this.uriProcessor.getVaultName(input.getUri());

        Artefact artefact = Artefact.builder()
                .uri(input.getUri())
                .build();

        return Try.of(() -> {
                    Vault vault = this.vaultRepository
                            .findVaultByName(vaultName)
                            .orElseThrow(IllegalArgumentException::new);

                    this.artefactRepository
                            .findArtefactByUri(input.getUri())
                            .map(vault::removeArtefact);
                    this.artefactRepository.deleteArtefactByUri(input.getUri());

                    Artefact persistedArtefact = this.artefactRepository.save(artefact);

                    vault.addArtefact(persistedArtefact);
                    this.vaultRepository.save(vault);

                    return input;
                })
                .toEither()
                .mapLeft(throwable -> FileUploadError.builder().build());
    }

    private Either<ApiError, Tuple2<String, String>> writeFile(UploadFileInput input) {
        return this.fileWriter
                .saveFileToDisk(input.getContent())
                .map(filename -> Tuple.of(filename, input.getUri()))
                .mapLeft(persistenceError -> FileUploadError.builder().message(persistenceError.getMessage()).build());
    }

    private Either<ApiError, UploadFileResult> updateFilename(Tuple2<String, String> filenameAndUri) {
        String filename = filenameAndUri._1;
        String uri = filenameAndUri._2;

        return Try.of(() -> {
                    Artefact artefact = this.artefactRepository
                            .findArtefactByUri(uri)
                            .map(foundArtefact -> foundArtefact.setFilename(filename))
                            .orElseThrow(IllegalArgumentException::new);

                    this.artefactRepository.save(artefact);

                    return UploadFileResult.builder().build();
                })
                .toEither()
                .mapLeft(throwable -> ServiceUnavailableError.builder().build());
    }

    private Boolean canCreateVault(UploadFileInput input) {
        String uriUsername = this.uriProcessor.getUsername(input.getUri());
        String authUsername = this.conversionService.convert(Optional.of(input.getAuthentication()), BasicAuth.class).getUsername();

        return uriUsername.equalsIgnoreCase(authUsername);
    }

    private boolean canUpdateVault(Vault vault) {
        MicroartUser currentUser = (MicroartUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getDetails();

        return vault
                .getAuthorizedUsers()
                .contains(currentUser);
    }
}
