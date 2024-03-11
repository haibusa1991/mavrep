package com.personal.microart.core.email.factory.emails;

import com.personal.microart.core.email.factory.BaseEmail;
import com.personal.microart.core.email.sender.Email;
import com.personal.microart.core.email.sender.EmailParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PasswordRecoveryEmail extends BaseEmail {
    private final String EMAIL_TEMPLATE_LOCATION = "/html/email/password-recovery.html";
    private final String PASSWORD_RECOVERY_URI_FORMAT = "%s/user/reset-password?token=%s";
    private final String EMAIL_SUBJECT = "Password Recovery";

    private final TemplateEngine templateEngine;

    @Value("${APPLICATION_URL}")
    private String applicationUrl;

    @Override
    public Email build(Map<EmailParameter, String> emailData) {
        final Context ctx = new Context();
        ctx.setVariable("username", emailData.get(EmailParameter.USERNAME));
        ctx.setVariable("passwordResetUrl", String.format(this.PASSWORD_RECOVERY_URI_FORMAT, this.applicationUrl, emailData.get(EmailParameter.PASSWORD_RECOVERY_TOKEN)));
        ctx.setVariable("tokenValidity", emailData.get(EmailParameter.TOKEN_VALIDITY));

        PasswordRecoveryEmail email = new PasswordRecoveryEmail(templateEngine);
        email.setTo(emailData.get(EmailParameter.TO));
        email.setSubject(this.EMAIL_SUBJECT);
        email.setHtmlBody(this.templateEngine.process(this.EMAIL_TEMPLATE_LOCATION, ctx));

        return email;
    }
}
