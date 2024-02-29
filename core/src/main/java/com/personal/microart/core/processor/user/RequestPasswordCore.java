package com.personal.microart.core.processor.user;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.errors.SilentFailError;
import com.personal.microart.api.operations.user.requestpassword.RequestPasswordInput;
import com.personal.microart.api.operations.user.requestpassword.RequestPasswordOperation;
import com.personal.microart.api.operations.user.requestpassword.RequestPasswordResult;
import com.personal.microart.core.email.base.Email;
import com.personal.microart.core.email.base.EmailGenerator;
import com.personal.microart.core.email.base.EmailParameter;
import com.personal.microart.core.email.base.EmailSender;
import com.personal.microart.core.recovery.PasswordRecoveryManager;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.repositories.UserRepository;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@Component
@RequiredArgsConstructor
public class RequestPasswordCore implements RequestPasswordOperation {
    private final UserRepository userRepository;
    private final PasswordRecoveryManager passwordRecoveryManager;
    private final EmailSender emailSender;

    @Qualifier("passwordRecoveryEmailGenerator")
    private final EmailGenerator emailGenerator;

    @Value("${PASSWORD_RECOVERY_TOKEN_VALIDITY}")
    private Integer TOKEN_VALIDITY;

    @Override
    public Either<ApiError, RequestPasswordResult> process(RequestPasswordInput input) {

        return this.getUser(input)
                .flatMap(user -> this.passwordRecoveryManager.getRecoveryToken(user).map(token -> Tuple.of(token, user)))
                .map(this::getEmail)
                .flatMap(this.emailSender::sendEmail)
                .map(response -> RequestPasswordResult.builder().build());
    }

    private Either<ApiError, MicroartUser> getUser(RequestPasswordInput input) {
        return Try.of(() -> this.userRepository.findByEmail(input.getEmail()).orElseThrow(IllegalArgumentException::new))
                .toEither()
                .mapLeft(throwable -> Match(throwable).of(
                        Case($(instanceOf(IllegalArgumentException.class)), SilentFailError::fromThrowable),
                        Case($(), ServiceUnavailableError::fromThrowable)
                ));
    }

    private Email getEmail(Tuple2<String, MicroartUser> tokenUserTuple) {
        String recoveryToken = tokenUserTuple._1;
        MicroartUser user = tokenUserTuple._2;

        HashMap<EmailParameter, String> emailData = new HashMap<>() {{
            put(EmailParameter.TO, user.getEmail());
            put(EmailParameter.USERNAME, user.getUsername());
            put(EmailParameter.PASSWORD_RECOVERY_TOKEN, recoveryToken);
            put(EmailParameter.TOKEN_VALIDITY, TOKEN_VALIDITY.toString());
        }};

        return this.emailGenerator.getEmail(emailData);
    }

}
