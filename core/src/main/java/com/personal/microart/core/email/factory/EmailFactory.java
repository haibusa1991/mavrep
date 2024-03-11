package com.personal.microart.core.email.factory;

import com.personal.microart.core.email.sender.Email;
import com.personal.microart.core.email.sender.EmailParameter;

import java.util.Map;

public interface EmailFactory {

    Email getPasswordRecoveryEmail(Map<EmailParameter, String> emailData);
}
