package com.personal.microart.core.auth.base;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
/**
 * This class represents a JWT authentication. Used for easy identification of the authentication type.
 */
public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {
    @Getter
    private final AuthenticationType type = AuthenticationType.JWT;

    public JwtAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
