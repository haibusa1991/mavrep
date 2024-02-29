package com.personal.microart.core.email.mailgun;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.core.email.base.Email;
import com.personal.microart.core.email.base.EmailSender;
import com.personal.microart.core.email.base.EmailSenderResponse;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class MailgunEmailSender implements EmailSender {
    @Value("${MAILGUN_API_KEY}")
    private String apiKey;

    @Value("${MAILGUN_DOMAIN}")
    private String domain;

    @Value("${MAILGUN_URL}")
    private String url;

    @Override
    public Either<ApiError, ? extends EmailSenderResponse> sendEmail(Email email) {
        WebClient client = WebClient.create();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("from", this.domain);
        body.add("to", email.getTo());
        body.add("subject", email.getSubject());
        body.add("html", email.getHtmlBody());

        return Try.of(() -> client.method(HttpMethod.POST)
                        .uri(this.url)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(("api:" + this.apiKey).getBytes()))
                        .bodyValue(body)
                        .retrieve()
                        .toEntity(MailgunResponse.class)
                        .block()
                        .getBody()
                )
                .toEither()
                .mapLeft(ServiceUnavailableError::fromThrowable);
    }
}
