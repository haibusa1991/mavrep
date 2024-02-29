package com.personal.microart.core.email.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
/**
 * The Email class represents an email with a recipient, subject, and HTML body.
 */
@Builder
@AllArgsConstructor
@Getter
public class Email {

    private String to;
    private String subject;
    private String htmlBody;
}
