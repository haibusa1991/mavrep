package com.personal.microart.validation.constraints;

import com.personal.microart.validation.validators.Rfc5322EmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * The string has to be a well-formed email address. Accepts {@code CharSequence}.
 * <p>
 * {@code null} elements are considered valid.
 *
 */
@Documented
@Constraint(validatedBy = Rfc5322EmailValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rfc5322Email {
    String message() default "{jakarta.validation.constraints.Email.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
