package com.personal.microart.core.auth;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Component
public class BasicAuthConverter {

    public BasicAuth getBasicAuth(String basicAuthHeader) {

        String[] credentials = Optional.ofNullable(basicAuthHeader)
                .map(rawHeader -> rawHeader.substring(6))
                .map(rawToken -> Base64.getDecoder().decode(rawToken))
                .map(tokenBytes -> new String(tokenBytes, StandardCharsets.UTF_8))
                .map(token -> token.split(":"))
                .orElse(new String[]{"", ""});

        return BasicAuth.builder()
                .username(credentials[0])
                .password(String.join("", Arrays.copyOfRange(credentials, 1, credentials.length)))
                .build();
    }
}
