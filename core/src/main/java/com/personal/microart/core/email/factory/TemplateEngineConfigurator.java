package com.personal.microart.core.email.factory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * This is used to configure the Thymeleaf template engine for email templates.
 */
@Configuration
public class TemplateEngineConfigurator {

    @Bean
    public TemplateEngine emailTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();

        templateEngine.addTemplateResolver(htmlTemplateResolver());

        return templateEngine;
    }
    /**
     * Creates a ClassLoaderTemplateResolver that is configured to resolve HTML templates.
     * Some default values are set. Each email specifies its own template location and data, but generally
     * "/resources/templates/html/email/EMAIL-TEMPLATE-NAME.html"
     *
     * @return a configured ClassLoaderTemplateResolver instance
     */
    private ITemplateResolver htmlTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setResolvablePatterns(Collections.singleton("templates/html/*"));
        templateResolver.setPrefix("/email/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setCacheable(false);
        return templateResolver;
    }
}
