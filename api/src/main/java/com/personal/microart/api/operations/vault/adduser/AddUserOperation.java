package com.personal.microart.api.operations.vault.adduser;


import com.personal.microart.api.base.Processor;
//TODO: update documentation
/**
 * The LoginOperation is processes the login request required for all front end related operations. Validates that the user
 * exists, is not disabled and the password is correct. Generates a JWT and returns it in a response object. The user
 * controller is responsible for setting the JWT in the response header. Returns the following errors:
 * <ul>
 *     <li>{@link com.personal.microart.api.errors.InvalidCredentialsError InvalidCredentialsError} if the credentials are not valid</li>
 *     <li>{@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if the user could not be retrieved from the database</li>
 * </ul>
 */

/**
 * The AddUserOperation adds a user to the list of authorized users of a vault. User must be existing and not disabled.
 * Only owners of the vault can add users. Returns the following errors:
 * <ul>
 *     <li>TODO: Add errors</li>
 * </ul>
 */
public interface AddUserOperation extends Processor<AddUserResult, AddUserInput> {

}
