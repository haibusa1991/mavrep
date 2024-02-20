package com.personal.microart.core.auth.jwt;


import com.auth0.jwt.algorithms.Algorithm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Token is a class that represents a JWT token.
 * It contains the email and username of the user, the issued at and expiry times of the token, and the signing algorithm used.
 */
@AllArgsConstructor
@Builder
@Getter
public class Token {

    private final String email;
    private final String username;
    private final Instant iat;
    private final Instant exp;
    private final Algorithm signAlgorithm;

    /**
     * Returns an empty token with empty email and username, current time as issued at and expiry times, and no signing algorithm.
     *
     * @return An empty token.
     */
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
