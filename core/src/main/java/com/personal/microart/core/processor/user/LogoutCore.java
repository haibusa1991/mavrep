package com.personal.microart.core.processor.user;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.errors.SilentFailError;
import com.personal.microart.api.operations.user.logout.LogoutInput;
import com.personal.microart.api.operations.user.logout.LogoutOperation;
import com.personal.microart.api.operations.user.logout.LogoutResult;
import com.personal.microart.core.auth.jwt.JwtProvider;
import com.personal.microart.core.auth.jwt.Token;
import com.personal.microart.persistence.entities.BlacklistedJwt;
import com.personal.microart.persistence.repositories.BlacklistedJwtRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.hibernate.JDBCException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@Component
@RequiredArgsConstructor
public class LogoutCore implements LogoutOperation {
    private final JwtProvider jwtProvider;
    private final BlacklistedJwtRepository blacklistedJwtRepository;


    //TODO: protect against brute force attacks
    @Override
    public Either<ApiError, LogoutResult> process(LogoutInput input) {

        return this.verifyHeader(input.getAuthentication())
                .flatMap(this::verifyIfBlacklisted)
                .flatMap(this::getToken)
                .flatMap(this::isExpired)
                .flatMap(token -> this.blacklist(input.getAuthentication(), token));
    }

    private Either<ApiError, String> verifyHeader(String rawHeader) {
        return this.jwtProvider.isValidJwt(rawHeader)
                ? Either.right(rawHeader)
                : Either.left(SilentFailError.builder().build());
    }

    private Either<ApiError, String> verifyIfBlacklisted(String rawHeader) {
        Boolean isBlacklisted = this.blacklistedJwtRepository.existsByToken(rawHeader.substring(7));

        return isBlacklisted
                ? Either.left(SilentFailError.builder().build())
                : Either.right(rawHeader);
    }

    private Either<ApiError, Token> getToken(String rawHeader) {
        return Try.of(() -> this.jwtProvider.getJwt(rawHeader))
                .toEither()
                .mapLeft(SilentFailError::fromThrowable);
    }

    private Either<ApiError, Token> isExpired(Token token) {
        Boolean isExpired = token.getExp().isBefore(Instant.now().atOffset(ZoneOffset.UTC).toInstant());

        return isExpired
                ? Either.left(SilentFailError.builder().build())
                : Either.right(token);
    }

    private Either<ApiError, LogoutResult> blacklist(String rawValue, Token token) {
        return Try.of(() -> {

                    BlacklistedJwt blacklistedJwt = BlacklistedJwt
                            .builder()
                            .token(rawValue.substring(7))
                            .validity(token.getExp().atOffset(ZoneOffset.UTC).toLocalDateTime())
                            .build();

                    this.blacklistedJwtRepository.save(blacklistedJwt);

                    return LogoutResult.builder().build();
                })
                .toEither()
                .mapLeft(throwable -> Match(throwable).of(
                        Case($(instanceOf(JDBCException.class)), ServiceUnavailableError.builder().build()),
                        Case($(), SilentFailError::fromThrowable)
                ));

    }
}
