package com.personal.microart.api.operations.user.resetpassword;


import com.personal.microart.api.base.Processor;

//TODO: replace statuses with appropriate ApiErrors and provide links to the classes
/**
 * The ResetPasswordOperation verifies the reset token validity, invalidates it and updates the user's password. Returns 204 if
 * request is successfully processed, 400 if the token and/or the new password is invalid, 401 if the reset token is
 * used or expired, 503 if the user or token could not be retrieved from the database.
 */
public interface ResetPasswordOperation extends Processor<ResetPasswordResult, ResetPasswordInput> {

}
