package com.personal.microart.core.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
public class BasicAuth {

    private final String username;
    private final String password;
}
