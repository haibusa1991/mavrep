package com.personal.mavrep.rest.controllers;

import com.personal.mavrep.api.base.ProcessorInput;
import com.personal.mavrep.api.errors.ApiError;
import com.personal.mavrep.api.operations.browse.BrowseInput;
import com.personal.mavrep.api.operations.user.register.RegisterInput;
import com.personal.mavrep.core.processor.user.RegisterCore;
import com.personal.mavrep.core.validator.InputValidator;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController extends BaseController {

    private final InputValidator inputValidator;
    private final RegisterCore register;

    @PostMapping(path = "/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody RegisterInput input, @Qualifier() HttpServletResponseImpl response) {

        Either<ApiError, ProcessorInput> validationResult = this.inputValidator.validateInput(input);
        if (validationResult.isLeft()) {
            return this.handle(validationResult, response);
        }

        return this.handle(this.register.process(input), response, HttpStatus.CREATED);
    }

}
