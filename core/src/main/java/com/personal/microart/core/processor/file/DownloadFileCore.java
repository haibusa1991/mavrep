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
import com.personal.microart.persistence.filehandler.FileReader;
import com.personal.microart.persistence.repositories.VaultRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static io.vavr.API.*;

/**
 * This component is responsible for file download operations. Gets the requested file uri and delegates to the file reader
 * to do the actual reading from the file system. Returns 404 if the file is not found or user is not authorized;
 * 503 if the file could not be read from disk.
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
        //TODO: refactor to look prettier

        String vaultName = this.extractor.getVaultName(input.getUri());

        Optional<Vault> vaultOptional = this.vaultRepository.findVaultByName(vaultName);

        if (vaultOptional.isEmpty()) {
            return Either.left(InvalidCredentialsError.builder().build());
        }

        Vault vault = vaultOptional.get();

        if(vault.isPublic()){
            return Either.right(input);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication instanceof AnonymousAuthenticationToken){
            return Either.left(InvalidCredentialsError.builder().build());
        }

        return vault.getAuthorizedUsers().contains((MicroartUser) authentication.getDetails())
                ? Either.right(input)
                : Either.left(InvalidCredentialsError.builder().build());
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
