package com.personal.microart.api.operations.user.resetpassword;


import com.personal.microart.api.base.Processor;

/**
 * The ResetPasswordOperation verifies the reset token validity, invalidates it and updates the user's password.
 * Returns the following errors:
 * <ul>
 *      <li>{@link com.personal.microart.api.errors.InvalidCredentialsError InvalidCredentialsError} if the token and/or the new password is invalid</li>
 *      <li>{@link com.personal.microart.api.errors.TokenInvalidError TokenInvalidError} if the reset token is used or expired</li>
 *      <li>{@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if the user or token could not be retrieved from the database</li>
 * </ul>
 */
public interface ResetPasswordOperation extends Processor<ResetPasswordResult, ResetPasswordInput> {

}
