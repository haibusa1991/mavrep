package com.personal.microart.core.processor.vault;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.DuplicateVaultNameError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.operations.vault.create.CreateVaultInput;
import com.personal.microart.api.operations.vault.create.CreateVaultOperation;
import com.personal.microart.api.operations.vault.create.CreateVaultResult;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.repositories.VaultRepository;
import io.vavr.API;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@Component
@RequiredArgsConstructor
public class CreateVaultCore implements CreateVaultOperation {
    private final VaultRepository vaultRepository;

    @Override
    public Either<ApiError, CreateVaultResult> process(CreateVaultInput input) {
        return this.validateNotExisting(input)
                .flatMap(this::createVault);
    }

    private Either<ApiError, Tuple2<String, MicroartUser>> validateNotExisting(CreateVaultInput input) {
        String vaultName = input.getVaultName();

        return Try.of(() -> {
                    MicroartUser user = (MicroartUser) SecurityContextHolder.getContext().getAuthentication().getDetails();

                    if (this.vaultRepository.existsByNameAndOwner(vaultName, user)) {
                        throw new IllegalArgumentException();
                    }

                    return Tuple.of(vaultName, user);
                })
                .toEither()
                .mapLeft(throwable -> API.Match(throwable).of(
                        Case($(instanceOf(IllegalArgumentException.class)), DuplicateVaultNameError::fromThrowable),
                        Case($(), ServiceUnavailableError::fromThrowable)
                ));
    }


    private Either<ApiError, CreateVaultResult> createVault(Tuple2<String, MicroartUser> vaultNameAndOwner) {
        String vaultName = vaultNameAndOwner._1;
        MicroartUser owner = vaultNameAndOwner._2;

        Vault vault = Vault
                .builder()
                .name(vaultName)
                .owner(owner)
                .build();

        return Try.of(() -> {
                    this.vaultRepository.save(vault);
                    return CreateVaultResult.builder().build();
                })
                .toEither()
                .mapLeft(ServiceUnavailableError::fromThrowable);
    }
}
