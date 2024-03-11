package com.personal.microart.core.email.sender;

import com.personal.microart.api.errors.ApiError;
import io.vavr.control.Either;

/**
 * An interface for sending emails. Concrete implementations of this interface should be able to send emails.
 */
public interface EmailSender {

    /**
     * Sends an email.
     *
     * @param email The email to be sent. This should be an instance of the Email class.
     */
    Either<ApiError, ? extends EmailSenderResponse> sendEmail(Email email);
}
