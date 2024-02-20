package com.personal.microart.core.converters;

import com.auth0.jwt.JWT;
import com.personal.microart.core.auth.jwt.Token;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TokenToString implements Converter<Token, String> {
    @Override
    public String convert(Token source) {
        return JWT.create()
                .withClaim("email", source.getEmail())
                .withClaim("username", source.getUsername())
                .withIssuedAt(source.getIat())
                .withExpiresAt(source.getExp())
                .sign(source.getSignAlgorithm());
    }
}
