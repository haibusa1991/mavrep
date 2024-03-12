package com.personal.microart.core.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.personal.microart.persistence.entities.MicroartUser;
import io.vavr.control.Try;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * JwtProvider is a component responsible for generating and validating JWT tokens.
 */
@Component
public class JwtProvider {
    @Value("${JWT_SECRET}")
    private String JWT_SECRET;

    @Value("${JWT_TOKEN_VALIDITY}")
    private String TOKEN_VALIDITY_DURATION;

    private Duration TOKEN_VALIDITY;

    @PostConstruct
    private void init() {
        this.TOKEN_VALIDITY = Duration.of(Long.parseLong(this.TOKEN_VALIDITY_DURATION), ChronoUnit.DAYS);
    }

    /**
     * Generates a JWT token for a given user.
     *
     * @param user The user for whom the token is to be generated.
     * @return The generated JWT token.
     */
    public Token getJwt(MicroartUser user) {
        return Token.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .iat(Instant.now())
                .exp(Instant.now().plus(this.TOKEN_VALIDITY))
                .signAlgorithm(Algorithm.HMAC256(this.JWT_SECRET))
                .build();
    }

    /**
     * Validates and decodes a JWT token from a raw header.
     *
     * @param rawHeader The raw header containing the JWT token.
     * @return The decoded JWT token if valid, otherwise an empty token.
     */
    public Token getJwt(String rawHeader) {

        if (!rawHeader.startsWith("Bearer ")) {
            return Token.empty();
        }

        return Try.of(() -> {
            DecodedJWT decoded = JWT
                    .require(Algorithm.HMAC256(this.JWT_SECRET))
                    .withClaimPresence("email")
                    .withClaimPresence("username")
                    .withClaimPresence("iat")
                    .withClaimPresence("exp")
                    .build()
                    .verify(rawHeader.substring(7));

            return Token.builder()
                    .email(decoded.getClaim("email").asString())
                    .username(decoded.getClaim("username").asString())
                    .iat(decoded.getIssuedAt().toInstant())
                    .exp(decoded.getExpiresAt().toInstant())
                    .signAlgorithm(Algorithm.HMAC256(this.JWT_SECRET))
                    .build();
        }).getOrElseGet(ignored -> Token.empty());
    }

    /**
     * Validates a JWT token from a raw header.
     * @param rawHeader The raw header containing the JWT token.
     * @return true if the token is valid, false otherwise.
     */

    public Boolean isValidJwt(String rawHeader) {
        return Try.of(() -> {
            if (rawHeader == null || !rawHeader.startsWith("Bearer ")) {
                return false;
            }

            JWT.require(Algorithm.HMAC256(this.JWT_SECRET))
                    .build()
                    .verify(rawHeader.substring(7));

            return true;
        }).getOrElseGet(ignored -> false);

    }
}