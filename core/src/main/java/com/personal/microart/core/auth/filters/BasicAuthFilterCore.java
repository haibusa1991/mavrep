package com.personal.microart.core.auth.filters;

import com.personal.microart.core.auth.basic.BasicAuth;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class BasicAuthFilterCore extends BaseFilterCore {
    private final PasswordEncoder passwordEncoder;
    private final ConversionService conversionService;
    private final ApplicationContext context;
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        super.setContext(this.context);
    }

    @Override
    public Boolean hasValidCredentials(HttpServletRequest request) {

        if(Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).isEmpty()
                || !request.getHeader(HttpHeaders.AUTHORIZATION).startsWith("Basic")) {
            return false;
        }

        BasicAuth auth = this.conversionService.convert(Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)), BasicAuth.class);

        return this.userRepository
                .findByUsername(auth.getUsername())
                .map(microartUser -> this.passwordEncoder.matches(auth.getPassword(), microartUser.getPassword()))
                .orElse(false);
    }

    @Override
    public UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        BasicAuth auth = this.conversionService.convert(Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)), BasicAuth.class);

        MicroartUser user = this.userRepository
                .findByUsername(auth.getUsername())
                .orElseThrow(IllegalArgumentException::new);

        UserDetails userDetails = new User(
                user.getUsername(),
                user.getPassword(),
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(user);

        return authToken;
    }
}
