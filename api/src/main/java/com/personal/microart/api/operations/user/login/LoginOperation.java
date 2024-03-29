package com.personal.microart.api.operations.user.login;


import com.personal.microart.api.base.Processor;

/**
 * The LoginOperation is processes the login request required for all front end related operations. Validates that the user
 * exists, is not disabled and the password is correct. Generates a JWT and returns it in a response object. The user
 * controller is responsible for setting the JWT in the response header. Returns the following errors:
 * <ul>
 *     <li>{@link com.personal.microart.api.errors.InvalidCredentialsError InvalidCredentialsError} if the credentials are not valid</li>
 *     <li>{@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if the user could not be retrieved from the database</li>
 * </ul>
 */
public interface LoginOperation extends Processor<LoginResult, LoginInput> {

}
