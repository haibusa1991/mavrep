package com.personal.microart.rest.controllers;

import com.personal.microart.api.base.ProcessorInput;
import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.operations.user.login.LoginInput;
import com.personal.microart.api.operations.user.login.LoginResult;
import com.personal.microart.api.operations.user.register.RegisterInput;
import com.personal.microart.core.processor.user.LoginCore;
import com.personal.microart.core.processor.user.RegisterCore;
import com.personal.microart.core.validator.InputValidator;
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

    private final InputValidator inputValidator;
    private final RegisterCore register;
    private final LoginCore login;
    private final ExchangeAccessor exchangeAccessor;

    @PostConstruct
    private void setExchangeAccessor() {
        super.setExchangeAccessor(exchangeAccessor);
    }

    @PostMapping(path = "/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody RegisterInput input, HttpServletResponse response) {

        Either<ApiError, ProcessorInput> validationResult = this.inputValidator.validateInput(input);
        if (validationResult.isLeft()) {
            return this.handle(validationResult, response);
        }

        return this.handle(this.register.process(input), response, HttpStatus.CREATED);
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

}
