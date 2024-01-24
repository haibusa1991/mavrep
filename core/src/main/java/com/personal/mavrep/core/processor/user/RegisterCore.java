package com.personal.mavrep.core.processor.user;

import com.personal.mavrep.api.errors.ApiError;
import com.personal.mavrep.api.errors.ConstraintViolationError;
import com.personal.mavrep.api.errors.ServiceUnavailableError;
import com.personal.mavrep.api.operations.user.register.RegisterInput;
import com.personal.mavrep.api.operations.user.register.RegisterOperation;
import com.personal.mavrep.api.operations.user.register.RegisterResult;
import com.personal.mavrep.persistence.repositories.UserRepository;
import com.personal.mavrep.persistence.entities.User;
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
        User user = User.builder()
                .email(input.getEmail())
                .password(this.passwordEncoder.encode(input.getPassword()))
                .build();

        return Try.of(() -> this.userRepository.save(user))
                .map(ignored -> RegisterResult.builder().build())
                .toEither()
                .mapLeft(throwable -> API.Match(throwable).of(
                        Case($(instanceOf(JDBCException.class)), exception -> ServiceUnavailableError.builder().build()),
                        Case($(instanceOf(DataIntegrityViolationException.class)), exception -> ConstraintViolationError.builder().statusMessage("email already registered").build())
                ));
    }
}
