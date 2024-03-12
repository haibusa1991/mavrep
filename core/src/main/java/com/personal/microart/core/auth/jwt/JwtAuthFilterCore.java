package com.personal.microart.core.auth.jwt;

import com.personal.microart.core.auth.base.BaseFilterCore;
import com.personal.microart.core.Extractor;
import com.personal.microart.core.auth.base.BasicAuthenticationToken;
import com.personal.microart.core.auth.base.JwtAuthenticationToken;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.repositories.BlacklistedJwtRepository;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * JwtAuthFilterCore handles the core logic of JWT authentication - whether Authorization header is present,
 * whether the token is valid, if the user is not disabled or the JWT is blacklisted.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilterCore extends BaseFilterCore {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final ApplicationContext context;
    private final BlacklistedJwtRepository blacklistedJwtRepository;

    @PostConstruct
    public void init() {
        super.setContext(this.context);
    }

    /**
     * Checks whether the request has an Authorization header, verifies if the value is a valid JWT,
     * and whether the JWT is not blacklisted. If all checks pass, it returns a
     * {@link JwtAuthenticationToken} with the user's details. If any of the checks fail, the current
     * AuthenticationToken is returned. By default, that is a
     * {@link org.springframework.security.authentication.AnonymousAuthenticationToken AnonymousAuthenticationToken}.
     * Since the JWT filter is after the BasicAuthFilter, it is possible that the user is already authenticated by a
     * {@link BasicAuthenticationToken}. In that case, the method will return the current token.
     *
     * @param request The HTTP request from which to retrieve the authentication information.
     * @return {@link JwtAuthenticationToken}
     */
    @Override
    public Authentication getAuthentication(HttpServletRequest request) {
        if (!this.isValidJwt(request) || this.isBlacklisted(request)) {
            return this.getCurrentToken();
        }

        MicroartUser user = this.getCurrentUser(request);

        if (user.isEmpty()) {
            return this.getCurrentToken();
        }

        return Optional.of(this.getUserDetails(user))
                .map(userDetails -> this.getAuthToken(userDetails, user))
                .orElseGet(this::getCurrentToken);
    }

    private Boolean isValidJwt(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(this.jwtProvider::isValidJwt)
                .orElse(false);
    }

    private Boolean isBlacklisted(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(rawHeader -> this.blacklistedJwtRepository.existsByToken(rawHeader.substring(7)))
                .orElse(false);
    }

    private MicroartUser getCurrentUser(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(this.jwtProvider::getJwt)
                .flatMap(jwt -> this.userRepository.findByUsername(jwt.getUsername()))
                .orElse(MicroartUser.empty());
    }

    private UserDetails getUserDetails(MicroartUser user) {
        Set<SimpleGrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(user.getUsername(), user.getPassword(), authorities);
    }

    private Authentication getAuthToken(UserDetails userDetails, MicroartUser user) {
        UsernamePasswordAuthenticationToken token = new JwtAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        token.setDetails(user);
        return token;
    }
}
