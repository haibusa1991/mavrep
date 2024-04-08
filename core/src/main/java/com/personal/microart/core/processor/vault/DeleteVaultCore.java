package com.personal.microart.core.processor.vault;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.errors.VaultNotFoundError;
import com.personal.microart.api.operations.vault.delete.DeleteVaultInput;
import com.personal.microart.api.operations.vault.delete.DeleteVaultOperation;
import com.personal.microart.api.operations.vault.delete.DeleteVaultResult;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@RequiredArgsConstructor
@Component
public class DeleteVaultCore implements DeleteVaultOperation {
    private final ArtefactRepository artefactRepository;
    private final VaultRepository vaultRepository;


    @Override
    public Either<ApiError, DeleteVaultResult> process(DeleteVaultInput input) {
        return this.validateExisting(input)
                .flatMap(this::deleteArtefacts)
                .flatMap(this::deleteVault);
    }

    private Either<ApiError, Vault> validateExisting(DeleteVaultInput input) {
        return Try.of(() -> {
                    MicroartUser user = (MicroartUser) SecurityContextHolder.getContext().getAuthentication().getDetails();
                    return this.vaultRepository
                            .findVaultByNameAndOwner(input.getVaultName(), user)
                            .orElseThrow(IllegalArgumentException::new);
                }).toEither()
                .mapLeft(throwable -> Match(throwable).of(
                        Case($(instanceOf(IllegalArgumentException.class)), VaultNotFoundError::fromThrowable),
                        Case($(), ServiceUnavailableError::fromThrowable)));
    }


    private Either<ApiError, Vault> deleteArtefacts(Vault vault) {
        return Try.of(() -> {
                    List<Artefact> artefactsToDelete = vault
                            .getArtefacts()
                            .stream()
                            .map(artefact -> artefact.setFilename(null))
                            .toList();

                    this.artefactRepository.saveAll(artefactsToDelete);

                    return vault;
                }).toEither()
                .mapLeft(ServiceUnavailableError::fromThrowable);
    }

    private Either<ApiError, DeleteVaultResult> deleteVault(Vault vault) {
        return Try.of(() -> {
                    this.vaultRepository.delete(vault);
                    return DeleteVaultResult.builder().build();
                }).toEither()
                .mapLeft(ServiceUnavailableError::fromThrowable);
    }
}
