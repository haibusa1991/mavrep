package com.personal.microart.core.recovery;

import com.personal.microart.core.email.base.Email;
import com.personal.microart.core.email.base.EmailGenerator;
import com.personal.microart.core.email.base.EmailParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Qualifier("dummyEmailGenerator")
public class DummyEmailGenerator implements EmailGenerator {
    @Override
    public Email getEmail(Map<EmailParameter, String> emailData) {
        return null;
    }
}
