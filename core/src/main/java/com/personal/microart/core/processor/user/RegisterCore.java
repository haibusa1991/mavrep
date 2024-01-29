package com.personal.microart.core.processor.user;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.ConstraintViolationError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.operations.user.register.RegisterInput;
import com.personal.microart.api.operations.user.register.RegisterOperation;
import com.personal.microart.api.operations.user.register.RegisterResult;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.persistence.entities.MicroartUser;
import io.vavr.API;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.hibernate.JDBCException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@Component
@RequiredArgsConstructor
public class RegisterCore implements RegisterOperation {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public Either<ApiError, RegisterResult> process(RegisterInput input) {
        MicroartUser user = MicroartUser.builder()
                .email(input.getEmail())
                .username(input.getUsername())
                .password(this.passwordEncoder.encode(input.getPassword()))
                .build();

        return Try.of(() -> this.userRepository.save(user))
                .map(ignored -> RegisterResult.builder().build())
                .toEither()
                .mapLeft(throwable -> API.Match(throwable).of(
                        Case($(instanceOf(JDBCException.class)), exception -> ServiceUnavailableError.builder().build()),
                        Case($(instanceOf(DataIntegrityViolationException.class)), exception -> ConstraintViolationError.builder().statusMessage("email or username already registered").build())
                ));
    }
}
