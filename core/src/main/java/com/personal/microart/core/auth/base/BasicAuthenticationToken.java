package com.personal.microart.core.auth.base;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * This class represents a basic authentication token. Used for easy identification of the authentication type.
 */
public class BasicAuthenticationToken extends UsernamePasswordAuthenticationToken {
    @Getter
    private final AuthenticationType type = AuthenticationType.BASIC;

    public BasicAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
