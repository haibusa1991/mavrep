package com.personal.microart.core.processor.file;

import com.personal.microart.api.errors.*;
import com.personal.microart.api.operations.file.upload.UploadFileInput;
import com.personal.microart.api.operations.file.upload.UploadFileOperation;
import com.personal.microart.api.operations.file.upload.UploadFileResult;
import com.personal.microart.core.Extractor;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.directorymanager.FileWriter;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * An {@link UploadFileOperation} implementation. Gets the requested file URI and contents and delegates
 * to the file writer component to do the actual writing to the file system. Returns the following errors:
 * <ul>
 *     <li>{@link ConstraintViolationError} if the filename is invalid or the file could not be written to disk</li>
 *     <li>{@link InvalidCredentialsError} if the user is not authorized to upload to the vault</li>
 *     <li>{@link FileUploadError} if the file could not be written to disk</li>
 *     <li>{@link ServiceUnavailableError} if the database is not available</li>
 * </ul>
 */
@RequiredArgsConstructor
@Component
@Transactional
public class UploadFileCore implements UploadFileOperation {
    private final FileWriter fileWriter;
    private final ArtefactRepository artefactRepository;
    private final VaultRepository vaultRepository;
    private final Extractor extractor;

    @Override
    public Either<ApiError, UploadFileResult> process(UploadFileInput input) {

        return this.validateVaultOwnership(input)
                .flatMap(this::validateFilename)
                .flatMap(this::createFileRecord)
                .flatMap(this::writeFile)
                .flatMap(this::updateFilename);
    }

    private Either<ApiError, UploadFileInput> validateVaultOwnership(UploadFileInput input) {
        return Try.of(() -> {
                    MicroartUser user = (MicroartUser) SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            .getDetails();

                    Boolean isOwnVault = this.extractor.getUsername(input.getUri()).equalsIgnoreCase(user.getUsername());

                    if (isOwnVault || isAuthorized(user, input.getUri())) {
                        return input;
                    }

                    throw new IllegalArgumentException();

                })
                .toEither()
                .mapLeft(InvalidCredentialsError::fromThrowable);
    }


    private Boolean isAuthorized(MicroartUser user, String uri) {
        return this.vaultRepository
                .findVaultByName(this.extractor.getVaultName(uri))
                .map(vault -> vault.getAuthorizedUsers().contains(user))
                .orElse(false);
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
                .mapLeft(throwable -> ConstraintViolationError.builder().statusMessage(throwable.getMessage()).build());
    }

    private Either<ApiError, UploadFileInput> createFileRecord(UploadFileInput input) {
        String vaultName = this.extractor.getVaultName(input.getUri());
        MicroartUser user = (MicroartUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getDetails();

        Artefact artefact = Artefact.builder()
                .uri(input.getUri())
                .build();

        return Try.of(() -> {
                    Vault vault = this.vaultRepository
                            .findVaultByName(vaultName)
                            .orElseGet(() -> this.vaultRepository.save(Vault.builder().name(vaultName).owner(user).build()));

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
                .mapLeft(ServiceUnavailableError::fromThrowable);
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
}
