package com.personal.microart.core.processor.vault;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.NotFoundError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.errors.UnauthorizedError;
import com.personal.microart.api.operations.vault.adduser.AddUserInput;
import com.personal.microart.api.operations.vault.adduser.AddUserOperation;
import com.personal.microart.api.operations.vault.adduser.AddUserResult;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@Component
@RequiredArgsConstructor
public class AddUserCore implements AddUserOperation {
    private final VaultRepository vaultRepository;
    private final UserRepository userRepository;

    @Override
    public Either<ApiError, AddUserResult> process(AddUserInput input) {

        return this.verifyOwnership(input)
                .flatMap(this::verifyAddedUserExists)
                .flatMap(this::getVault)
                .flatMap(this::addUserToVault);
    }

    private Either<ApiError, AddUserInput> verifyOwnership(AddUserInput input) {
        return Try.of(() -> {
                    MicroartUser currentUser = (MicroartUser) SecurityContextHolder.getContext().getAuthentication().getDetails();

                    return this.vaultRepository
                            .findVaultByName(input.getVaultName())
                            .map(Vault::getOwner)
                            .filter(vaultOwner -> vaultOwner.equals(currentUser))
                            .map(ignored -> input)
                            .orElseThrow(IllegalArgumentException::new);

                })
                .toEither()
                .mapLeft(UnauthorizedError::fromThrowable);
    }

    private Either<ApiError, Tuple2<MicroartUser, String>> verifyAddedUserExists(AddUserInput input) {

        return Try.of(() -> this.userRepository
                        .findByUsername(input.getUsername())
                        .filter(MicroartUser::enabled)
                        .map(targetUser -> Tuple.of(targetUser, input.getVaultName()))
                        .orElseThrow(() -> new IllegalArgumentException("User does not exist")))
                .toEither()
                .mapLeft(throwable -> Match(throwable).of(
                        Case($(instanceOf(IllegalArgumentException.class)), NotFoundError::fromThrowable),
                        Case($(), ServiceUnavailableError::fromThrowable)
                ));

    }

    private Either<ApiError, Tuple2<Vault, MicroartUser>> getVault(Tuple2<MicroartUser, String> targetUserAndVaultName) {
        MicroartUser targetUser = targetUserAndVaultName._1;
        String vaultName = targetUserAndVaultName._2;

        return Try.of(() -> this.vaultRepository
                        .findVaultByName(vaultName)
                        .map(vault -> Tuple.of(vault, targetUser))
                        .orElseThrow(() -> new IllegalArgumentException("Vault does not exist")))
                .toEither()
                .mapLeft(throwable -> Match(throwable).of(
                        Case($(instanceOf(IllegalArgumentException.class)), NotFoundError::fromThrowable),
                        Case($(), ServiceUnavailableError::fromThrowable)
                ));
    }

    private Either<ApiError, AddUserResult> addUserToVault(Tuple2<Vault, MicroartUser> vaultAndTargetUser) {
        Vault vault = vaultAndTargetUser._1;
        MicroartUser targetUser = vaultAndTargetUser._2;

        vault.addUser(targetUser);

        return Try.of(() -> Optional.of(this.vaultRepository.save(vault))
                        .map(ignored -> AddUserResult.builder().build())
                        .orElseThrow(IllegalStateException::new))
                .toEither()
                .mapLeft(ServiceUnavailableError::fromThrowable);
    }
}
