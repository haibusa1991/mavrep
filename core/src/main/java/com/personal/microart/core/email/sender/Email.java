package com.personal.microart.core.email.sender;

import java.util.Map;

public interface Email {
    Email build(Map<EmailParameter, String> emailData);

    String getTo();

    String getSubject();

    String getHtmlBody();
}
