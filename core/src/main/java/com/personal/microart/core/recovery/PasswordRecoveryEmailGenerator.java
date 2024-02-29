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
@Qualifier("passwordRecoveryEmailGenerator")
public class PasswordRecoveryEmailGenerator implements EmailGenerator {
    private final TemplateEngine templateEngine;

    private final String EMAIL_TEMPLATE_LOCATION = "/html/email/password-recovery.html";
    private final String PASSWORD_RECOVERY_URI_FORMAT = "%s/user/reset-password?token=%s";
    private final String EMAIL_SUBJECT = "Password Recovery";


    @Value("${APPLICATION_URL}")
    private String applicationUrl;

    @Override
    public Email getEmail(Map<EmailParameter, String> emailData) {

        final Context ctx = new Context();
        ctx.setVariable("username", emailData.get(EmailParameter.USERNAME));
        ctx.setVariable("passwordResetUrl", String.format(this.PASSWORD_RECOVERY_URI_FORMAT, this.applicationUrl, emailData.get(EmailParameter.PASSWORD_RECOVERY_TOKEN)));
        ctx.setVariable("tokenValidity", emailData.get(EmailParameter.TOKEN_VALIDITY));

        return Email.builder()
                .to(emailData.get(EmailParameter.TO))
                .subject(this.EMAIL_SUBJECT)
                .htmlBody(this.templateEngine.process(this.EMAIL_TEMPLATE_LOCATION, ctx))
                .build();
    }


}
