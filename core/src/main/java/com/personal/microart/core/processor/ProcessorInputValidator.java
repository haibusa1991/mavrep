package com.personal.microart.core.processor;

import com.personal.microart.api.base.ProcessorInput;
import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.ConstraintViolationError;
import io.vavr.control.Either;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

import java.util.List;

/**
 * This class is responsible for validating the input of any processor. Wraps the input in an Either object by
 * collecting all the ConstraintValidation exceptions and returning them as an ApiError. If no errors are found, the
 * input is returned as a right Either. Custom ValidatorFactory is used to enable Spring DI in the custom validators
 * (e.g. @Rfc5322Email)
 *
 */
@Component
public class ProcessorInputValidator {
    private final Validator validator;

    public ProcessorInputValidator(final AutowireCapableBeanFactory autowireCapableBeanFactory) {
//        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
//            this.validator = validatorFactory.getValidator();
//        }

        this.validator = Validation
                .byProvider(HibernateValidator.class)
                .configure()
                .constraintValidatorFactory(new SpringConstraintValidatorFactory(autowireCapableBeanFactory))
                .buildValidatorFactory()
                .getValidator();
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