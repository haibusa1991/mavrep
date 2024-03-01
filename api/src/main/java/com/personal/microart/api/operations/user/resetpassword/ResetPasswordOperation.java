package com.personal.microart.api.operations.user.resetpassword;


import com.personal.microart.api.base.Processor;

/**
 * The ResetPasswordOperation verifies the reset token validity and updates the user's password. Returns 204 if
 * request is successfully processed, 400 if the new password is invalid, 401 if the reset token is invalid,
 * 503 if the user could not be retrieved from the database.
 */
public interface ResetPasswordOperation extends Processor<ResetPasswordResult, ResetPasswordInput> {

}
