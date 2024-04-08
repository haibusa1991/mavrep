package com.personal.microart.rest.controllers;

import com.personal.microart.api.base.ProcessorInput;
import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.operations.file.upload.UploadFileInput;
import com.personal.microart.api.operations.user.login.LoginInput;
import com.personal.microart.api.operations.user.login.LoginOperation;
import com.personal.microart.api.operations.user.login.LoginResult;
import com.personal.microart.api.operations.user.logout.LogoutInput;
import com.personal.microart.api.operations.user.logout.LogoutOperation;
import com.personal.microart.api.operations.user.register.RegisterInput;
import com.personal.microart.api.operations.user.register.RegisterOperation;
import com.personal.microart.api.operations.user.requestpassword.RequestPasswordInput;
import com.personal.microart.api.operations.user.requestpassword.RequestPasswordOperation;
import com.personal.microart.api.operations.user.resetpassword.ResetPasswordInput;
import com.personal.microart.api.operations.user.resetpassword.ResetPasswordOperation;
import com.personal.microart.api.operations.user.verifypassordresettoken.VerifyPasswordResetTokenInput;
import com.personal.microart.api.operations.user.verifypassordresettoken.VerifyPasswordResetTokenOperation;
import com.personal.microart.core.processor.ProcessorInputValidator;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.vavr.control.Either;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import static com.personal.microart.rest.Endpoints.*;

/**
 * A controller that is responsible for handling requests related to user accounts - registration, login, password reset, etc.
 */
@RestController
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final ProcessorInputValidator inputValidator;
    private final RegisterOperation register;
    private final LoginOperation login;
    private final ExchangeAccessor exchangeAccessor;
    private final RequestPasswordOperation requestPassword;
    private final VerifyPasswordResetTokenOperation verifyPasswordResetToken;
    private final ResetPasswordOperation resetPassword;
    private final LogoutOperation logout;

    @PostConstruct
    private void setExchangeAccessor() {
        super.setExchangeAccessor(exchangeAccessor);
    }

    @PostMapping(path = USER_REGISTER)
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody RegisterInput input, HttpServletResponse response) {

        Either<ApiError, ProcessorInput> validationResult = this.inputValidator.validateInput(input);

        return validationResult.isLeft()
                ? this.handle(validationResult, response)
                : this.handle(this.register.process(input), response, HttpStatus.CREATED);
    }

    @PostMapping(path = USER_LOGIN)
    public ResponseEntity<?> login(@RequestBody LoginInput input, HttpServletResponse response) {

        Either<ApiError, ProcessorInput> validationResult = this.inputValidator.validateInput(input);
        if (validationResult.isLeft()) {
            return this.handle(validationResult, response);
        }

        Either<ApiError, LoginResult> loginAttempt = this.login.process(input);

        if (loginAttempt.isRight()) {
            response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + loginAttempt.get().getJwt());
            return ResponseEntity.status(204).build();
        }

        return this.handle(loginAttempt, response);
    }

    @PostMapping(path = USER_REQUEST_PASSWORD)
    public ResponseEntity<?> requestPassword(@RequestBody RequestPasswordInput input, HttpServletResponse response) {

        Either<ApiError, ProcessorInput> validationResult = this.inputValidator.validateInput(input);

        return validationResult.isLeft()
                ? this.handle(validationResult, response)
                : this.handle(this.requestPassword.process(input), response);
    }

    @GetMapping(path = USER_RESET_PASSWORD)
    public ResponseEntity<?> passwordResetVerify(@RequestParam String passwordResetToken, HttpServletResponse response) {

        VerifyPasswordResetTokenInput input = VerifyPasswordResetTokenInput
                .builder()
                .resetToken(passwordResetToken)
                .build();

        Either<ApiError, ProcessorInput> validationResult = this.inputValidator.validateInput(input);

        return validationResult.isLeft()
                ? this.handle(validationResult, response)
                : this.handle(this.verifyPasswordResetToken.process(input), response, HttpStatus.NO_CONTENT);
    }

    @PostMapping(path = USER_RESET_PASSWORD)
    public ResponseEntity<?> passwordReset(@RequestParam String passwordResetToken,
                                           @RequestBody ResetPasswordInput rawInput,
                                           HttpServletResponse response) {

        ResetPasswordInput input = ResetPasswordInput.builder()
                .resetToken(passwordResetToken)
                .password(rawInput.getPassword())
                .build();

        Either<ApiError, ProcessorInput> validationResult = this.inputValidator.validateInput(input);

        return validationResult.isLeft()
                ? this.handle(validationResult, response)
                : this.handle(this.resetPassword.process(input), response, HttpStatus.NO_CONTENT);
    }

    @SecurityRequirement(name = "Authorization")
    @PostMapping(path = USER_LOGOUT)
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        LogoutInput input = LogoutInput
                .builder()
                .authentication(request.getHeader(HttpHeaders.AUTHORIZATION))
                .build();

        return this.handle(this.logout.process(input), response);
    }

}
