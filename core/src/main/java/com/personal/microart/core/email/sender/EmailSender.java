package com.personal.microart.core.email.sender;

import com.personal.microart.api.errors.ApiError;
import io.vavr.control.Either;

/**
 * Represents an email sender. Any class that sends emails should implement this interface.
 */
public interface EmailSender {

    /**
     * Sends an email.
     *
     * @param email The {@link Email Email} to be sent.
     */
    Either<ApiError, ? extends EmailSenderResponse> sendEmail(Email email);
}
