package com.personal.microart.core.email.base;

import java.util.Map;
/**
 * The EmailGenerator interface provides a method for generating an {@link Email} object.
 * The getEmail method takes a Map of EmailParameter and String as an argument, which represents the data needed to generate the email.
 */
public interface EmailGenerator {

    Email getEmail(Map<EmailParameter, String> emailData);
}
