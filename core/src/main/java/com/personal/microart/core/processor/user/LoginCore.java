package com.personal.microart.core.processor.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.InvalidCredentialsError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.operations.user.login.LoginInput;
import com.personal.microart.api.operations.user.login.LoginOperation;
import com.personal.microart.api.operations.user.login.LoginResult;
import com.personal.microart.core.auth.jwt.JwtProvider;
import com.personal.microart.core.auth.jwt.Token;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.repositories.UserRepository;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@Component
@RequiredArgsConstructor
public class LoginCore implements LoginOperation {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ConversionService conversionService;

    @Override
    public Either<ApiError, LoginResult> process(LoginInput input) {
        return this.getUser(input)
                .flatMap(this::validatePassword)
                .map(this::getResult);
    }

    private Either<ApiError, Tuple2<String, MicroartUser>> getUser(LoginInput input) {
        return Try.of(() -> Tuple.of(
                        input.getPassword(),
                        this.userRepository.findByEmail(input.getEmail()).orElseThrow(IllegalArgumentException::new))
                )
                .toEither()
                .mapLeft(throwable -> Match(throwable).of(
                        Case($(instanceOf(IllegalArgumentException.class)), InvalidCredentialsError.builder().build()),
                        Case($(), ServiceUnavailableError.builder().build())
                ));
    }

    private Either<ApiError, MicroartUser> validatePassword(Tuple2<String, MicroartUser> rawPasswordAndUserTuple) {
        String rawPassword = rawPasswordAndUserTuple._1;
        MicroartUser user = rawPasswordAndUserTuple._2;

        return this.passwordEncoder.matches(rawPassword, user.getPassword())
                ? Either.right(user)
                : Either.left(InvalidCredentialsError.builder().build());
    }

    private LoginResult getResult(MicroartUser user) {
        Token token = this.jwtProvider.getJwt(user);

        return LoginResult
                .builder()
                .jwt(this.conversionService.convert(token, String.class))
                .build();
    }
}
