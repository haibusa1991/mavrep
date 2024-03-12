package com.personal.microart.core.auth.basic;

import lombok.Builder;
import lombok.Getter;

/**
 * POJO containing username and password for basic authentication information.
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