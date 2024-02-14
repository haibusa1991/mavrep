package com.personal.microart.core.auth.jwt;


import com.auth0.jwt.algorithms.Algorithm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
@Builder
@Getter
public class Token {

    private final String email;
    private final String username;
    private final Instant iat;
    private final Instant exp;
    private final Algorithm signAlgorithm;

    public static Token empty() {
        return Token.builder()
                .email("")
                .username("")
                .iat(Instant.now())
                .exp(Instant.now())
                .signAlgorithm(Algorithm.none())
                .build();
    }
}
