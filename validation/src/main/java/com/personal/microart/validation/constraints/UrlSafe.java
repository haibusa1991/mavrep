package com.personal.microart.validation.constraints;

import com.personal.microart.validation.validators.UrlSafeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * The string has to be url safe. Accepts {@code CharSequence}. Allowed characters are:
 * <ul>
 *     <li>lowercase letters</li>
 *     <li>numbers 0-9</li>
 *     <li>hyphen</li>
 *     <li>underscore</li>
 *     <li>tilde</li>
 * <p>
 * {@code null} and empty elements are not considered valid.
 *
 */
@Documented
@Constraint(validatedBy = UrlSafeValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlSafe {
    String message() default "{com.personal.microart.validation.constraints.UrlSafe.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
