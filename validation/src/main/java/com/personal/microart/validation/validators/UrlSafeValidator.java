package com.personal.microart.validation.validators;

import com.personal.microart.validation.constraints.UrlSafe;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;
import java.util.regex.Pattern;

public class UrlSafeValidator implements ConstraintValidator<UrlSafe, String> {
    @Override
    public void initialize(UrlSafe constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        return Pattern
                .compile("[a-z0-9-_~]*$")
                .matcher(value.toLowerCase())
                .matches();
    }
}
