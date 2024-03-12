package com.personal.microart.api.operations.user.register;


import com.personal.microart.api.base.Processor;

/**
 * The RegisterOperation is used to register a new user. Saves the user in the database and returns a result object.
 * Returns the following errors:
 * <ul>
 *     <li>{@link com.personal.microart.api.errors.ConstraintViolationError ConstraintViolationError} if the email or username is already registered</li>
 *     <li>{@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if the database is not available</li>
 * </ul>
 */
public interface RegisterOperation extends Processor<RegisterResult, RegisterInput> {

}
