package com.personal.microart.core.auth.basic;

import lombok.Builder;
import lombok.Getter;

/**
 * This class represents the basic authentication credentials.
 * It contains the username and password for basic authentication.
 */
@Builder
@Getter
public class BasicAuth {

    private final String username;
    private final String password;

    public static BasicAuth empty() {
        return BasicAuth
                .builder()
                .username("")
                .password("")
                .build();
    }
}