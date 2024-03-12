package com.personal.microart.core.auth.basic;

import com.personal.microart.core.auth.base.BaseFilterCore;
import com.personal.microart.core.auth.base.BasicAuthenticationToken;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.repositories.UserRepository;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * BasicAuthFilterCore handles the core logic of basic authentication - whether Authorization header is present,
 * whether the user exists and whether the password is correct.
 */
@Component
@RequiredArgsConstructor
public class BasicAuthFilterCore extends BaseFilterCore {
    private final ConversionService conversionService;
    private final ApplicationContext context;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    private void init() {
        super.setContext(this.context);
    }

    /**
     * Checks whether the request has an Authorization header, decodes it and checks whether the user exists,
     * whether it is not disabled, and if the password is correct. If all checks pass, it returns a
     * {@link BasicAuthenticationToken} with the user's details. If any of the checks fail, the current
     * AuthenticationToken is returned. By default, that is a
     * {@link org.springframework.security.authentication.AnonymousAuthenticationToken AnonymousAuthenticationToken}.
     *
     * @param request The HTTP request from which to retrieve the authentication information.
     * @return {@link BasicAuthenticationToken}
     */
    @Override
    public Authentication getAuthentication(HttpServletRequest request) {
        MicroartUser user = this.getCurrentUser(request);

        if (user.isEmpty()) {
            return this.getCurrentToken();
        }

        return Optional.of(this.getUserDetails(user))
                .map(userDetails -> this.getAuthToken(userDetails, user))
                .orElseGet(this::getCurrentToken);
    }

    private MicroartUser getCurrentUser(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(rawHeader -> this.conversionService.convert(rawHeader, BasicAuth.class))
                .flatMap(auth -> this.userRepository.findByUsername(auth.getUsername()).map(user -> Tuple.of(user, auth))
                        .flatMap(this::verifyPassword))
                .orElseGet(MicroartUser::empty);
    }

    private Optional<MicroartUser> verifyPassword(Tuple2<MicroartUser, BasicAuth> userAndAuth) {
        String userPassword = userAndAuth._1.getPassword();
        String rawPassword = userAndAuth._2.getPassword();

        return this.passwordEncoder.matches(rawPassword, userPassword)
                ? Optional.of(userAndAuth._1)
                : Optional.empty();
    }

    private UserDetails getUserDetails(MicroartUser user) {
        return new User(
                user.getUsername(),
                user.getPassword(),
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private Authentication getAuthToken(UserDetails userDetails, MicroartUser user) {
        UsernamePasswordAuthenticationToken token = new BasicAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        token.setDetails(user);
        return token;
    }

}