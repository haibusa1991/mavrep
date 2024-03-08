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
 * The JwtAuthFilterCore class is a component in the application that handles the authorization and authentication of
 * HTTP requests using JWT (JSON Web Token). It extends the BaseFilterCore class and provides implementations for the
 * isAuthorized and getAuthentication methods.<br>
 * The isAuthorized method checks whether an incoming HTTP request is authorized. It allows all requests to the "/browse"
 * endpoint and unrestricted download access to all public vaults. For private vaults, it checks if the JWT token
 * in the request header corresponds to a user who is authorized to access the vault.<br>
 * The getAuthentication method authenticates a user based on the JWT token present in the request header.
 * If the token is valid and corresponds to an existing user, it creates a new authentication token for the user.
 * If the token is not present or invalid, it returns the current authentication token from the security context.
 * The current token should be an AnonymousAuthenticationToken if the user hasn't been authenticated in some other way.
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
