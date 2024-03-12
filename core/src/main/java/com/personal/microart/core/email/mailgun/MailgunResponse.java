package com.personal.microart.core.email.mailgun;

import com.personal.microart.core.email.sender.EmailSenderResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Email sender response for Mailgun
 */
@NoArgsConstructor
@Getter
public class MailgunResponse implements EmailSenderResponse {
    private String id;
    private String message;
}
