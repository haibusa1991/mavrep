package com.personal.microart.rest.controllers;

import com.personal.microart.api.base.ProcessorInput;
import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.operations.user.login.LoginInput;
import com.personal.microart.api.operations.user.login.LoginOperation;
import com.personal.microart.api.operations.user.login.LoginResult;
import com.personal.microart.api.operations.user.register.RegisterInput;
import com.personal.microart.api.operations.user.register.RegisterOperation;
import com.personal.microart.api.operations.user.requestpassword.RequestPasswordInput;
import com.personal.microart.api.operations.user.requestpassword.RequestPasswordOperation;
import com.personal.microart.api.operations.verifypassordresettoken.VerifyPasswordResetTokenInput;
import com.personal.microart.api.operations.verifypassordresettoken.VerifyPasswordResetTokenOperation;
import com.personal.microart.core.processor.ProcessorInputValidator;
import io.vavr.control.Either;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController extends BaseController {

    private final ProcessorInputValidator inputValidator;
    private final RegisterOperation register;
    private final LoginOperation login;
    private final ExchangeAccessor exchangeAccessor;
    private final RequestPasswordOperation requestPassword;
    private final VerifyPasswordResetTokenOperation verifyPasswordResetToken;

    @PostConstruct
    private void setExchangeAccessor() {
        super.setExchangeAccessor(exchangeAccessor);
    }

    @PostMapping(path = "/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody RegisterInput input, HttpServletResponse response) {

        Either<ApiError, ProcessorInput> validationResult = this.inputValidator.validateInput(input);

        return validationResult.isLeft()
                ? this.handle(validationResult, response)
                : this.handle(this.register.process(input), response, HttpStatus.CREATED);
    }

    @PostMapping(path = "/login")
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

    @PostMapping(path = "/request-password")
    public ResponseEntity<?> requestPassword(@RequestBody RequestPasswordInput input, HttpServletResponse response) {

        Either<ApiError, ProcessorInput> validationResult = this.inputValidator.validateInput(input);

        return validationResult.isLeft()
                ? this.handle(validationResult, response)
                : this.handle(this.requestPassword.process(input), response);
    }

    @GetMapping(path = "/password-reset")
    public ResponseEntity<?> passwordResetVerify(@RequestParam("token") String passwordResetToken, HttpServletResponse response) {

        VerifyPasswordResetTokenInput input = VerifyPasswordResetTokenInput
                .builder()
                .resetToken(passwordResetToken)
                .build();

        Either<ApiError, ProcessorInput> validationResult = this.inputValidator.validateInput(input);

        return validationResult.isLeft()
                ? this.handle(validationResult, response)
                : this.handle(this.verifyPasswordResetToken.process(input), response, HttpStatus.NO_CONTENT);
    }

}
