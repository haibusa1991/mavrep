package com.personal.microart.core.email.base;

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
 * This class is used to configure the Thymeleaf template engine for email templates.
 */
@Configuration
public class TemplateEngineConfigurator {

    /**
     * This method creates and configures a SpringTemplateEngine bean.
     *
     * @return a configured SpringTemplateEngine instance
     */
    @Bean
    public TemplateEngine emailTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();

        templateEngine.addTemplateResolver(htmlTemplateResolver());

        return templateEngine;
    }
    /**
     * This method creates a ClassLoaderTemplateResolver that is configured to resolve HTML templates.
     * Template location as follows: "/resources/templates/html/email/EMAIL-TEMPLATE-NAME.html" The template name
     * is specified in the EmailGenerator implementation, along with any data required by the template.
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
