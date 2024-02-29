package com.personal.microart.core.recovery;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.PasswordRecoveryToken;
import com.personal.microart.persistence.repositories.PasswordRecoveryTokenRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class Base64PasswordRecoveryManager implements PasswordRecoveryManager {
    private final PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;
    private final Random random = new SecureRandom();

    @Value("${PASSWORD_RECOVERY_TOKEN_VALIDITY}")
    private Integer TOKEN_VALIDITY;

    @Override
    @Transactional
    public Either<ApiError, String> getRecoveryToken(MicroartUser user) {
        return this.invalidateExistingTokens(user)
                .flatMap(this::createRecoveryTokenRecord)
                .map(PasswordRecoveryToken::getValue);
    }

    private String generatePasswordResetToken() {
        byte[] bytes = new byte[33];
        this.random.nextBytes(bytes);

        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    private Either<ApiError, MicroartUser> invalidateExistingTokens(MicroartUser user) {
        return Try.of(() -> {
                    this.passwordRecoveryTokenRepository
                            .findByUserAndIsValidTrue(user)
                            .map(PasswordRecoveryToken::invalidate)
                            .ifPresent(this.passwordRecoveryTokenRepository::save);

                    return user;
                })
                .toEither()
                .mapLeft(ServiceUnavailableError::fromThrowable);
    }

    private Either<ApiError, PasswordRecoveryToken> createRecoveryTokenRecord(MicroartUser user) {
        return Try.of(() -> {
                    PasswordRecoveryToken token = PasswordRecoveryToken
                            .builder()
                            .user(user)
                            .value(this.generatePasswordResetToken())
                            .tokenValidity(this.TOKEN_VALIDITY)
                            .build();

                    return this.passwordRecoveryTokenRepository.save(token);
                })
                .toEither()
                .mapLeft(ServiceUnavailableError::fromThrowable);
    }
}
