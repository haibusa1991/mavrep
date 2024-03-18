package com.personal.microart.core.processor.vault;

import com.personal.microart.api.errors.*;
import com.personal.microart.api.operations.vault.adduser.AddUserResult;
import com.personal.microart.api.operations.vault.removeuser.RemoveUserInput;
import com.personal.microart.api.operations.vault.removeuser.RemoveUserOperation;
import com.personal.microart.api.operations.vault.removeuser.RemoveUserResult;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.hibernate.JDBCException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static io.vavr.API.*;
import static io.vavr.API.$;
import static io.vavr.Predicates.instanceOf;

@Component
@RequiredArgsConstructor
public class RemoveUserCore implements RemoveUserOperation {
    private final VaultRepository vaultRepository;
    private final UserRepository userRepository;

    @Override
    public Either<ApiError, RemoveUserResult> process(RemoveUserInput input) {

        return this.verifyOwnership(input)
                .flatMap(this::verifyRemovedUserExists)
                .flatMap(this::getVault)
                .flatMap(this::removeUserFromVault);
    }

    private Either<ApiError, RemoveUserInput> verifyOwnership(RemoveUserInput input) {
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

    private Either<ApiError, Tuple2<MicroartUser, String>> verifyRemovedUserExists(RemoveUserInput input) {

        return Try.of(() -> this.userRepository
                        .findByUsername(input.getUsername())
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

    private Either<ApiError, RemoveUserResult> removeUserFromVault(Tuple2<Vault, MicroartUser> vaultAndTargetUser) {
        Vault vault = vaultAndTargetUser._1;
        MicroartUser targetUser = vaultAndTargetUser._2;

        Set<MicroartUser> authorizedUsers = vault.getAuthorizedUsers();

        return Try.of(() -> {
                    if (authorizedUsers.contains(targetUser) && authorizedUsers.size() == 1) {
                        throw new IllegalStateException();
                    }

                    vault.removeUser(targetUser);

                    return Optional.of(this.vaultRepository.save(vault))
                            .map(ignored -> RemoveUserResult.builder().build())
                            .orElseThrow(IllegalArgumentException::new);
                })
                .toEither()
                .mapLeft(throwable -> Match(throwable).of(
                        Case($(instanceOf(IllegalStateException.class)), ConstraintViolationError.builder().statusMessage("Vault must have at least one authorized user").build()),
                        Case($(instanceOf(JDBCException.class)), ServiceUnavailableError::fromThrowable),
                        Case($(), ServiceUnavailableError::fromThrowable)));
    }
}
