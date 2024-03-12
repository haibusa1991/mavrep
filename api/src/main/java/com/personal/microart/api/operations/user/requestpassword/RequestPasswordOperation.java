package com.personal.microart.api.operations.user.requestpassword;

import com.personal.microart.api.base.Processor;

/**
 * Handles requests for password reset. User should provide email address to receive a password reset link.
 * If the email address is found and user is not disabled, a single use reset token is generated and sent to the user.
 * The token is saved to the database. Expired token means that the token has been used and/or
 * validity has expired. A token is valid for 30 minutes by default.
 * The {@link com.personal.microart.core.scheduling.tasks.InvalidPasswordRecoveryTokensDeleter InvalidPasswordRecoveryTokensDeleter}
 * is responsible for cleaning up of the expired tokens.
 * Returns the follwing errors:
 *  <ul>
 *      <li>{@link com.personal.microart.api.errors.SilentFailError SilentFailError} if the email address is not found in the database.</li>
 *      <li>{@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if the email cannot be sent.</li>
 * </ul>
 */
public interface RequestPasswordOperation extends Processor<RequestPasswordResult, RequestPasswordInput> {

}