package com.personal.microart.api.operations.verifypassordresettoken;


import com.personal.microart.api.base.Processor;

/**
 * The VerifyPasswordResetToken verifies the reset token validity. Returns 200 if token is valid,
 * 400 if input is invalid, 401 if the reset token is invalid, 503 if the token could not be retrieved from the database.
 */
public interface VerifyPasswordResetTokenOperation extends Processor<VerifyPasswordResetTokenResult, VerifyPasswordResetTokenInput> {

}
