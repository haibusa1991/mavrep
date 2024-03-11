package com.personal.microart.core.processor.user;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.errors.SilentFailError;
import com.personal.microart.api.operations.user.requestpassword.RequestPasswordInput;
import com.personal.microart.api.operations.user.requestpassword.RequestPasswordOperation;
import com.personal.microart.api.operations.user.requestpassword.RequestPasswordResult;
import com.personal.microart.core.email.sender.EmailParameter;
import com.personal.microart.core.email.sender.EmailSender;
import com.personal.microart.core.email.sender.Email;
import com.personal.microart.core.email.factory.EmailFactory;
import com.personal.microart.core.recovery.PasswordRecoveryManager;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.repositories.UserRepository;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

/**
 * This is RequestPasswordInput implementation. EmailGenerator must be explicitly defined in the constructor.
 * In case the user provides email that does not exist in the database, the application will return a SilentFailError.
 * This is a special case that returns 204, but it is still an error. The application will return 503 in case of any
 * other error, e.g. EmailSender is unable to send an email.
 */

@Component
@RequiredArgsConstructor
public class RequestPasswordCore implements RequestPasswordOperation {
    private final UserRepository userRepository;
    private final PasswordRecoveryManager passwordRecoveryManager;
    private final EmailSender emailSender;
    private final EmailFactory emailFactory;

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

        return this.emailFactory.getPasswordRecoveryEmail(emailData);
    }

}
