package com.personal.microart.api.operations.vault.removeuser;


import com.personal.microart.api.base.Processor;
/**
 * The RemoveUserOperation removes a user from the list of authorized users of a vault. User must be existing.
 * Only vault owners can remove users. Returns the following errors:
 * <ul>
 *     <li>{@link com.personal.microart.api.errors.UnauthorizedError UnauthorizedError} if the user who tries to remove another user is unauthorized</li>
 *     <li>{@link com.personal.microart.api.errors.NotFoundError NotFoundError} if the user that has to be removed doesn't exist or the target vault doesn't exist </li>
 *     <li>{@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if a user or a vault could not be retrieved from the database</li>
 * </ul>
 */
public interface RemoveUserOperation extends Processor<RemoveUserResult, RemoveUserInput> {

}
