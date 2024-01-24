package com.personal.mavrep.core.validator;

import com.personal.mavrep.api.base.ProcessorInput;
import com.personal.mavrep.api.errors.ApiError;
import io.vavr.control.Either;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class Validator {
        private final Validator validator;

        public Validator() {
            ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
            this.validator = validatorFactory.getValidator();
        }

        public Either<ApiError, ProcessorInput> validateInput (ProcessorInput input) {

            List<String> errors = validator
                    .validate(input)
                    .stream()
                    .map(this::parseConstraintViolation)
                    .toList();

            if (errors.isEmpty()) {
                return Either.right(input);
            }

            return Either.left(ConstraintViolationError
                    .builder()
                    .statusMessage(String.join(",", errors))
                    .build());
        }

        private String parseConstraintViolation(ConstraintViolation<?> violation) {
            String path = violation.getPropertyPath().toString();
            String field = path.substring(path.indexOf('.') + 1);
            String message = violation.getMessage();

            if (violation
                    .getMessageTemplate()
                    .startsWith("{jakarta.validation.constraints.") && violation.getMessageTemplate()
                    .endsWith("message}")) {
                return field + " " + message;
            }

            return message;
        }
    }

}
