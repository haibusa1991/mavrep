package com.personal.microart.core.processor.file;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.FileNotFoundError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.operations.file.download.DownloadFileInput;
import com.personal.microart.api.operations.file.download.DownloadFileOperation;
import com.personal.microart.api.operations.file.download.DownloadFileResult;
import com.personal.microart.core.auth.BasicAuth;
import com.personal.microart.core.auth.BasicAuthConverter;
import com.personal.microart.core.processor.UriProcessor;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.errors.Error;
import com.personal.microart.persistence.filehandler.FileReader;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.hibernate.JDBCException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;


@RequiredArgsConstructor
@Component
public class DownloadFileCore implements DownloadFileOperation {
    private final FileReader fileReader;
    private final UserRepository userRepository;
    private final BasicAuthConverter authConverter;
    private final PasswordEncoder passwordEncoder;
    private final UriProcessor uriProcessor;
    private final VaultRepository vaultRepository;

    @Override
    public Either<ApiError, DownloadFileResult> process(DownloadFileInput input) {

        return this.validatePermissions(input)
                .flatMap(this::readFile);
    }

    private Either<ApiError, DownloadFileInput> validatePermissions(DownloadFileInput input) {

        String vaultName = this.uriProcessor.getVaultName(input.getUri());

        return Try.of(() -> {
                    Vault vault = this.vaultRepository
                            .findVaultByName(vaultName)
                            .orElseThrow(IllegalArgumentException::new);

                    if (vault.isPublic()) {
                        return input;
                    }

                    BasicAuth auth = this.authConverter.getBasicAuth(input.getAuthentication());
                    MicroartUser user = this.userRepository
                            .findByUsername(auth.getUsername())
                            .orElseThrow(IllegalArgumentException::new);

                    Boolean hasValidCredentials = this.passwordEncoder.matches(auth.getPassword(), user.getPassword());
                    Boolean isAuthorized = vault.getAuthorizedUsers().contains(user);

                    if (!hasValidCredentials || !isAuthorized) {
                        throw new IllegalArgumentException();
                    }

                    return input;
                })
                .toEither()
                .mapLeft(throwable -> Match(throwable).of(
                        Case($(instanceOf(IllegalArgumentException.class)), exception -> FileNotFoundError.builder().build()),
                        Case($(instanceOf(JDBCException.class)), exception -> ServiceUnavailableError.builder().build())
                ));
    }

    public Either<ApiError, DownloadFileResult> readFile(DownloadFileInput input) {

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
