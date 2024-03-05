package com.personal.microart.core.processor.user;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.errors.TokenInvalidError;
import com.personal.microart.api.operations.user.resetpassword.ResetPasswordInput;
import com.personal.microart.api.operations.user.resetpassword.ResetPasswordOperation;
import com.personal.microart.api.operations.user.resetpassword.ResetPasswordResult;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.PasswordRecoveryToken;
import com.personal.microart.persistence.repositories.PasswordRecoveryTokenRepository;
import com.personal.microart.persistence.repositories.UserRepository;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@Component
@RequiredArgsConstructor
public class ResetPasswordCore implements ResetPasswordOperation {
    private final UserRepository userRepository;
    private final PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public Either<ApiError, ResetPasswordResult> process(ResetPasswordInput input) {
        return this.getToken(input)
                .flatMap(this::verifyToken)
                .flatMap(this::invalidateToken)
                .flatMap(this::updatePassword);
    }

    private Either<ApiError, Tuple2<PasswordRecoveryToken, String>> getToken(ResetPasswordInput input) {
        return Try.of(() -> passwordRecoveryTokenRepository
                        .findByTokenValue(input.getResetToken())
                        .map(token -> new Tuple2<>(token, input.getPassword()))
                        .orElseThrow(IllegalArgumentException::new))
                .toEither()
                .mapLeft(this::handleException);
    }

    private Either<ApiError, Tuple2<PasswordRecoveryToken, String>> verifyToken(Tuple2<PasswordRecoveryToken, String> tokenAndPassword) {
        PasswordRecoveryToken token = tokenAndPassword._1;
        String password = tokenAndPassword._2;

        return Try.of(() -> {
                    Boolean isValid = token.getIsValid();
                    Boolean isExpired = token.getValidUntil().isBefore(LocalDateTime.now(ZoneOffset.UTC));

                    if (!isValid || isExpired) {
                        throw new IllegalArgumentException();
                    }

                    return Tuple.of(token, password);
                })
                .toEither()
                .mapLeft(this::handleException);
    }

    private Either<ApiError, Tuple2<PasswordRecoveryToken, String>> invalidateToken(Tuple2<PasswordRecoveryToken, String> tokenAndPassword) {
        PasswordRecoveryToken token = tokenAndPassword._1;
        String password = tokenAndPassword._2;

        return Try.of(() -> {
                    token.invalidate();
                    passwordRecoveryTokenRepository.save(token);

                    return Tuple.of(token, password);
                })
                .toEither()
                .mapLeft(this::handleException);
    }

    private Either<ApiError, ResetPasswordResult> updatePassword(Tuple2<PasswordRecoveryToken, String> tokenAndPassword) {
        PasswordRecoveryToken token = tokenAndPassword._1;
        String password = tokenAndPassword._2;

        return Try.of(() -> {
                    MicroartUser user = token.getUser();
                    user.setPassword(passwordEncoder.encode(password));
                    this.userRepository.save(user);

                    return ResetPasswordResult.builder().build();
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
