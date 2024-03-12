package com.personal.microart.api.operations.user.verifypassordresettoken;


import com.personal.microart.api.base.Processor;

/**
 * Verifies a reset token's validity. Returns the following errors:
 * <ul>
 *     <li>{@link com.personal.microart.api.errors.TokenInvalidError TokenInvalidError} if the input is invalid</li>
 *     <li>{@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if the token could not be retrieved from the database</li>
 * </ul>
 */
public interface VerifyPasswordResetTokenOperation extends Processor<VerifyPasswordResetTokenResult, VerifyPasswordResetTokenInput> {

}
