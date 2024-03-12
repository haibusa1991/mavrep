package com.personal.microart.core.email.factory;

import com.personal.microart.core.email.sender.Email;
import com.personal.microart.core.email.sender.EmailParameter;

import java.util.Map;
/**
 * The EmailFactory provides methods for creating different types of emails. Each method takes a
 * Map<EmailParameter, String> which must contain all variables required and returns an Email object.
 * Sample map for password recovery email:
 * <pre>
 *     Map<EmailParameter, String> emailData = new HashMap<>() {{
 *         put(EmailParameter.TO, "my.user@abv.bg");
 *         put(EmailParameter.SUBJECT, "Password recovery");
 *         put(EmailParameter.USERNAME, "MyUser");
 *         put(EmailParameter.PASSWORD_RECOVERY_TOKEN, "fdiuh_gGDSdsf2");
 *         put(EmailParameter.TOKEN_VALIDITY, "30");
 *     }};
 * </pre>
 */
public interface EmailFactory {
    /**
     * Creates a password recovery email. Requires the following parameters:
     * <ul>
     *     <li>{@link com.personal.microart.core.email.sender.EmailParameter#TO}: The recipient's email address</li>
     *     <li>{@link com.personal.microart.core.email.sender.EmailParameter#SUBJECT}: The subject of the email</li>
     *     <li>{@link com.personal.microart.core.email.sender.EmailParameter#USERNAME}: The username of the recipient.</li>
     *     <li>{@link com.personal.microart.core.email.sender.EmailParameter#PASSWORD_RECOVERY_TOKEN}: The password recovery token.</li>
     *     <li>{@link com.personal.microart.core.email.sender.EmailParameter#TOKEN_VALIDITY}: The validity of the password recovery token</li>
     * </ul>
     *
     * @param emailData A map containing the parameters for the email. The keys are of type EmailParameter,
     *                  which could represent various aspects of the email such as the recipient's address,
     *                  the subject of the email, etc. The values are the corresponding details for these parameters.
     * @return An Email object representing the password recovery email.
     */
    Email getPasswordRecoveryEmail(Map<EmailParameter, String> emailData);
}
