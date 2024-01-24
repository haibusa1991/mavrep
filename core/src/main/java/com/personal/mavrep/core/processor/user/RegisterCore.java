package com.personal.mavrep.core.processor.user;

import com.personal.mavrep.api.errors.ApiError;
import com.personal.mavrep.api.errors.ServiceUnavailableError;
import com.personal.mavrep.api.operations.user.register.RegisterInput;
import com.personal.mavrep.api.operations.user.register.RegisterOperation;
import com.personal.mavrep.api.operations.user.register.RegisterResult;
import com.personal.mavrep.persistence.repositories.UserRepository;
import com.personal.mavrep.persistence.entities.User;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
                .mapLeft(throwable -> ServiceUnavailableError.builder().build());
    }
}
