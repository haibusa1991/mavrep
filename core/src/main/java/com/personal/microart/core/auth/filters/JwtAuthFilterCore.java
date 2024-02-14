package com.personal.microart.core.auth.filters;

import com.personal.microart.core.auth.jwt.JwtProvider;
import com.personal.microart.core.auth.jwt.Token;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class JwtAuthFilterCore extends BaseFilterCore {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final ApplicationContext context;

    @PostConstruct
    public void init() {
        super.setContext(this.context);
    }

    @Override
    public Boolean hasValidCredentials(HttpServletRequest request) {
        if(Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).isEmpty()
                || !request.getHeader(HttpHeaders.AUTHORIZATION).startsWith("Bearer")) {
            return false;
        }

        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
//                .map(rawHeader -> rawHeader.substring(7))
                .map(this.jwtProvider::getJwt)
                .isPresent();
    }

    @Override
    public UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String rawJwt = request.getHeader(HttpHeaders.AUTHORIZATION).substring(7);
        Token token = this.jwtProvider.getJwt(rawJwt);

        MicroartUser user = this.userRepository
                .findByUsername(token.getUsername())
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
