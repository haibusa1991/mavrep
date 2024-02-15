package com.personal.microart.core.converters;

import com.personal.microart.core.auth.basic.BasicAuth;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Component
public class StringToBasicAuth implements Converter<Optional<String>, BasicAuth> {

    @Override
    public BasicAuth convert(Optional<String> source) {

        return source
                .map(rawHeader -> rawHeader.substring(6))
                .map(rawToken -> Base64.getDecoder().decode(rawToken))
                .map(tokenBytes -> new String(tokenBytes, StandardCharsets.UTF_8))
                .map(token -> token.split(":"))
                .map(this::buildToken)
                .orElse(null);
    }

    private BasicAuth buildToken(String[] tokenElements) {
        return BasicAuth.builder()
                .username(tokenElements[0])
                .password(String.join("", Arrays.copyOfRange(tokenElements, 1, tokenElements.length)))
                .build();
    }
}
