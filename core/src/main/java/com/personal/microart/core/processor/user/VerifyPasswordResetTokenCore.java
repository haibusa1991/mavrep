package com.personal.microart.core.processor.user;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.errors.TokenInvalidError;
import com.personal.microart.api.operations.user.verifypassordresettoken.VerifyPasswordResetTokenOperation;
import com.personal.microart.api.operations.user.verifypassordresettoken.VerifyPasswordResetTokenInput;
import com.personal.microart.api.operations.user.verifypassordresettoken.VerifyPasswordResetTokenResult;
import com.personal.microart.persistence.entities.PasswordRecoveryToken;
import com.personal.microart.persistence.repositories.PasswordRecoveryTokenRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@Component
@RequiredArgsConstructor
public class VerifyPasswordResetTokenCore implements VerifyPasswordResetTokenOperation {
    private final PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

    @Override
    public Either<ApiError, VerifyPasswordResetTokenResult> process(final VerifyPasswordResetTokenInput input) {

        return this.getToken(input)
                .flatMap(this::verifyToken)
                .map(ignored -> new VerifyPasswordResetTokenResult());
    }

    private Either<ApiError, PasswordRecoveryToken> getToken(final VerifyPasswordResetTokenInput input) {
        return Try.of(() -> passwordRecoveryTokenRepository
                        .findByTokenValue(input.getResetToken())
                        .orElseThrow(IllegalArgumentException::new))
                .toEither()
                .mapLeft(this::handleException);
    }

    private Either<ApiError, Boolean> verifyToken(final PasswordRecoveryToken token) {
        return Try.of(() -> {
                    Boolean isValid = token.getIsValid();
                    Boolean isExpired = token.getValidUntil().isBefore(LocalDateTime.now(ZoneOffset.UTC));

                    if (!isValid || isExpired) {
                        throw new IllegalArgumentException();
                    }

                    return true;
                })
                .toEither()
                .mapLeft(this::handleException);
    }

    private ApiError handleException(Throwable throwable) {
        return Match(throwable).of(
                Case($(instanceOf(IllegalArgumentException.class)), TokenInvalidError::fromThrowable),
                Case($(), ServiceUnavailableError::fromThrowable));
    }
}
