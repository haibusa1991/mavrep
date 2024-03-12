package com.personal.microart.core.processor.file;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.FileNotFoundError;
import com.personal.microart.api.errors.InvalidCredentialsError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.operations.file.download.DownloadFileInput;
import com.personal.microart.api.operations.file.download.DownloadFileOperation;
import com.personal.microart.api.operations.file.download.DownloadFileResult;
import com.personal.microart.core.Extractor;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.errors.Error;
import com.personal.microart.persistence.directorymanager.FileReader;
import com.personal.microart.persistence.repositories.VaultRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static io.vavr.API.*;

/**
 * A {@link DownloadFileOperation} implementation. Gets the requested file uri and delegates to the file reader
 * to do the actual reading from the file system. Returns the following errors:
 * <ul>
 *     <li>{@link FileNotFoundError} if the file is not found</li>
 *     <li>{@link InvalidCredentialsError} if the user is not authorized to access the file</li>
 *     <li>{@link ServiceUnavailableError} if the file could not be read from disk or database not available</li>
 * </ul>
 */
@RequiredArgsConstructor
@Component
public class DownloadFileCore implements DownloadFileOperation {
    private final FileReader fileReader;
    private final Extractor extractor;
    private final VaultRepository vaultRepository;

    @Override
    public Either<ApiError, DownloadFileResult> process(DownloadFileInput input) {
        return verifyPermissions(input)
                .flatMap(this::downloadFile);
    }

    private Either<ApiError, DownloadFileInput> verifyPermissions(DownloadFileInput input) {
        String vaultName = this.extractor.getVaultName(input.getUri());

        return this.vaultRepository.findVaultByName(vaultName)
                .map(vault -> verifyVaultPermissions(vault, input))
                .orElse(Either.left(InvalidCredentialsError.builder().build()));
    }

    private Either<ApiError, DownloadFileInput> verifyVaultPermissions(Vault vault, DownloadFileInput input) {
        return Try.of(() -> {
                    if (vault.isPublic()) {
                        return input;
                    }

                    MicroartUser currentUser = (MicroartUser) SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            .getDetails();

                    return Optional.of(vault.getAuthorizedUsers().contains(currentUser))
                            .filter(contains -> contains)
                            .map(ignored -> input)
                            .orElseThrow(IllegalArgumentException::new);
                })
                .toEither()
                .mapLeft(error -> InvalidCredentialsError.builder().build());
    }

    private Either<ApiError, DownloadFileResult> downloadFile(DownloadFileInput input) {
        return this.fileReader.readFile(input.getUri())
                .map(content -> {
                    String[] uriElements = input.getUri().split("/");
                    String filename = uriElements[uriElements.length - 1];

                    return DownloadFileResult.builder().content(content).filename(filename).build();
                })
                .mapLeft(error -> Match(error.getError()).of(
                        Case($(Error.READ_ERROR), readError -> ServiceUnavailableError.builder().build()),
                        Case($(Error.FILE_NOT_FOUND_ERROR), readError -> FileNotFoundError.builder().build())));
    }
}
