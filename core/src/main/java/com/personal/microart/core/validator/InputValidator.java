package com.personal.microart.core.validator;

import com.personal.microart.api.base.ProcessorInput;
import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.ConstraintViolationError;
import io.vavr.control.Either;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InputValidator {
    private final Validator validator;

    public InputValidator() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            this.validator = validatorFactory.getValidator();
        }
    }

    public Either<ApiError, ProcessorInput> validateInput(ProcessorInput input) {

        List<String> errors = validator
                .validate(input)
                .stream()
                .map(this::parseConstraintViolation)
                .toList();

        return errors.isEmpty()
                ? Either.right(input)
                : Either.left(ConstraintViolationError
                .builder()
                .statusMessage(String.join(", ", errors))
                .build());
    }

    private String parseConstraintViolation(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        String field = path.substring(path.indexOf('.') + 1);
        String message = violation.getMessage();

        return field + " " + message;
    }
}