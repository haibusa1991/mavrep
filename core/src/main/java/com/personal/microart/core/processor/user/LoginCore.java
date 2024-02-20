package com.personal.microart.core.processor.user;

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
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

/**
 * This is the login operation implementation. It is responsible for processing the login input. If credentials are
 * valid, it returns a JWT in a response object. Controller is responsible for setting the JWT in the response header.
 */
@Component
@RequiredArgsConstructor
public class LoginCore implements LoginOperation {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ConversionService conversionService;

    /**
     * This method processes the login operation.
     * It first retrieves the user, validates the password, and then generates the login result. Returns an 403 if
     * credentials are invalid, 503 if the user could not be retrieved from the database.
     *
     * @param input The login input containing the user's email and password.
     * @return Either an ApiError or a LoginResult.
     */
    @Override
    public Either<ApiError, LoginResult> process(LoginInput input) {
        return this.getUser(input)
                .flatMap(this::validatePassword)
                .map(this::getResult);
    }

    /**
     * This method retrieves the user from the repository using the email provided in the login input.
     * If the user is not found, it throws an IllegalArgumentException which is then mapped to an ApiError.
     *
     * @param input The login input containing the user's email and password.
     * @return Either an ApiError or a Tuple containing the raw password and the user.
     */
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

    /**
     * This method validates the password provided in the login input against the password stored in the user entity.
     * If the passwords do not match, it returns an InvalidCredentialsError.
     *
     * @param rawPasswordAndUserTuple A Tuple containing the raw password and the user.
     * @return Either an ApiError or the user.
     */
    private Either<ApiError, MicroartUser> validatePassword(Tuple2<String, MicroartUser> rawPasswordAndUserTuple) {
        String rawPassword = rawPasswordAndUserTuple._1;
        MicroartUser user = rawPasswordAndUserTuple._2;

        return this.passwordEncoder.matches(rawPassword, user.getPassword())
                ? Either.right(user)
                : Either.left(InvalidCredentialsError.builder().build());
    }

    /**
     * Generates a JWT for the user and returns it in a LoginResult. Controller is responsible for setting the JWT in the
     * response header.
     *
     * @param user The user for whom the JWT is to be generated.
     * @return The login result containing the JWT.
     */
    private LoginResult getResult(MicroartUser user) {
        Token token = this.jwtProvider.getJwt(user);

        return LoginResult
                .builder()
                .jwt(this.conversionService.convert(token, String.class))
                .build();
    }
}