package com.personal.microart.core.converters;

import com.personal.microart.core.auth.basic.BasicAuth;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Component
public class StringToBasicAuth implements Converter<String, BasicAuth> {

    @Override
    public BasicAuth convert(String source) {
        if (source.isEmpty() || !this.isValidBasicAuth(source)) {
            return BasicAuth.empty();
        }

        return Optional.of(source)
                .map(rawHeader -> rawHeader.substring(6))
                .map(rawToken -> Base64.getDecoder().decode(rawToken))
                .map(tokenBytes -> new String(tokenBytes, StandardCharsets.UTF_8))
                .map(token -> token.split(":"))
                .map(this::buildToken)
                .orElse(BasicAuth.empty());
    }

    private BasicAuth buildToken(String[] tokenElements) {
        return BasicAuth.builder()
                .username(tokenElements[0])
                .password(String.join("", Arrays.copyOfRange(tokenElements, 1, tokenElements.length)))
                .build();
    }

    private Boolean isValidBasicAuth(String rawHeader) {
        return rawHeader.toLowerCase().startsWith("basic ")
                && rawHeader.split(" ").length == 2
                && this.isBase64(rawHeader.split(" ")[1])
                && new String(Base64.getDecoder().decode(rawHeader.split(" ")[1])).split(":").length >= 2;
    }

    private boolean isBase64(String header) {
        try {
            Base64.getDecoder().decode(header);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
