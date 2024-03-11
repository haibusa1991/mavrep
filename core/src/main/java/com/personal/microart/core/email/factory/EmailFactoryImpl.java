package com.personal.microart.core.email.factory;

import com.personal.microart.core.email.sender.Email;
import com.personal.microart.core.email.sender.EmailParameter;
import com.personal.microart.core.email.factory.emails.PasswordRecoveryEmail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailFactoryImpl implements EmailFactory {
    private final PasswordRecoveryEmail passwordRecoveryEmail;

    @Override
    public Email getPasswordRecoveryEmail(Map<EmailParameter, String> emailData) {
        return passwordRecoveryEmail.build(emailData);
    }
}
