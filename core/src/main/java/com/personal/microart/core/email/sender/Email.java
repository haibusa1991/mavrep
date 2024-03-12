package com.personal.microart.core.email.sender;

import java.util.Map;
/**
 * Represents an email that can be sent by an {@link EmailSender EmailSender}.
 */
public interface Email {

    /**
     * Builds the email with the given data. Required by the {@link com.personal.microart.core.email.factory.EmailFactory EmailFactory}
     *
     * @param emailData the data to build the email with
     * @return the built Email object
     */
    Email build(Map<EmailParameter, String> emailData);

    /**
     * @return the recipient's email address
     */
    String getTo();

    /**
     * @return the subject of the email
     */
    String getSubject();

    /**
     * @return the HTML body of the email
     */
    String getHtmlBody();
}
