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
 * This component is responsible for validating the input of any processor.Collects all the ConstraintValidation
 * exceptions and returning them as an {@link ConstraintViolationError}. If no errors are found, the input is returned.
 * Custom ValidatorFactory is used to enable Spring DI for custom validators, e.g.
 * {@link com.personal.microart.validation.constraints.Rfc5322Email @Rfc5322Email}
 *
 */
@Component
public class ProcessorInputValidator {
    private final Validator validator;

    public ProcessorInputValidator(final AutowireCapableBeanFactory autowireCapableBeanFactory) {
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