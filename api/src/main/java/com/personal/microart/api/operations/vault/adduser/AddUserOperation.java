package com.personal.microart.api.operations.vault.adduser;


import com.personal.microart.api.base.Processor;
/**
 * The AddUserOperation adds a user to the list of authorized users of a vault. User must be existing and not disabled.
 * Only owners of the vault can add users. Returns the following errors:
 * <ul>
 *     <li>{@link com.personal.microart.api.errors.UnauthorizedError UnauthorizedError} if the user who tries to add another user is unauthorized</li>
 *     <li>{@link com.personal.microart.api.errors.NotFoundError NotFoundError} if the user that has to be added doesn't exist or the target vault doesn't exist </li>
 *     <li>{@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if a user or a vault could not be retrieved from the database</li>
 * </ul>
 */
public interface AddUserOperation extends Processor<AddUserResult, AddUserInput> {

}
