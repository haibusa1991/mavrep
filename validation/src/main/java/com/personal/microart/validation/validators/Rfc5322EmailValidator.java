package com.personal.microart.validation.validators;

import com.personal.microart.validation.constraints.Rfc5322Email;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class Rfc5322EmailValidator implements ConstraintValidator<Rfc5322Email, String> {
    @Override
    public void initialize(Rfc5322Email constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return Pattern
                .compile("^((?:[A-Za-z0-9!#$%&'*+\\-\\/=?^_`{|}~]|(?<=^|\\.)\"|\"(?=$|\\.|@)|(?<=\".*)[ .](?=.*\")|(?<!\\.)\\.){1,64})(@)((?:[A-Za-z0-9.\\-])*(?:[A-Za-z0-9])\\.(?:[A-Za-z0-9]){2,})$")
                .matcher(value)
                .matches();
    }
}
