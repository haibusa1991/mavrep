package com.personal.microart.core.processor.file;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.FileUploadError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.errors.ValidationError;
import com.personal.microart.api.operations.file.upload.UploadFileInput;
import com.personal.microart.api.operations.file.upload.UploadFileOperation;
import com.personal.microart.api.operations.file.upload.UploadFileResult;
import com.personal.microart.core.processor.UriProcessor;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.filehandler.FileWriter;
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
 * This component is responsible for file upload operations. Gets the requested file uri and contents and delegates
 * to the file writer component to do the actual writing to the file system. Returns 400 if filename is invalid;
 * 409 if the artefact is already deployed; 503 if the file could not be written to disk.
 */
@RequiredArgsConstructor
@Component
@Transactional
public class UploadFileCore implements UploadFileOperation {
    private final FileWriter fileWriter;
    private final ArtefactRepository artefactRepository;
    private final VaultRepository vaultRepository;
    private final UriProcessor uriProcessor;


/**
 * Processes the upload file input and returns the upload file result.
 * This method performs the following operations in order:<br>
 * 1. Validates the filename of the file to be uploaded.<br>
 * 2. Creates a record for the file in the database. Filename remains blank since it will be updated once the file is
 * successfully uploaded. <br>
 * 3. Writes the file to the disk. The writing is delegated to the FileWriter component. Result is a UUID. <br>
 * 4. Adds the filename to the record from step 2.<br>
 *
 * @param input UploadFileInput object containing the target URI and the file contents as  byte[].
 * @return Either an ApiError or an UploadFileResult.
 */
@Override
public Either<ApiError, UploadFileResult> process(UploadFileInput input) {

    return this.validateFilename(input)
            .flatMap(this::createFileRecord)
            .flatMap(this::writeFile)
            .flatMap(this::updateFilename);
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
                            .orElseGet(() -> this.vaultRepository.save(Vault.builder().name(vaultName).user(user).build()));

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
}
