package com.personal.microart.core.auth;

import com.personal.microart.persistence.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BasicAuthFilterCore {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<UsernamePasswordAuthenticationToken> getSecurityToken(HttpServletRequest request) {

        String[] credentials = Optional.ofNullable(request.getHeader("Authorization"))
                .map(rawHeader -> rawHeader.substring(6))
                .map(rawToken -> Base64.getDecoder().decode(rawToken))
                .map(tokenBytes -> new String(tokenBytes, StandardCharsets.UTF_8))
                .map(token -> token.split(":"))
                .orElse(new String[]{"", ""});

        String username = credentials[0];
        String password = String.join("", Arrays.copyOfRange(credentials, 1, credentials.length));

        Optional<UsernamePasswordAuthenticationToken> authTokenOptional = this.userRepository
                .findByUsername(username)
                .map(userEntity -> new User(userEntity.getUsername(), userEntity.getPassword(), Set.of(new SimpleGrantedAuthority("ROLE_USER"))))
                .map(user -> new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        if (authTokenOptional.isPresent() && isValidPassword(authTokenOptional.get(), password)) {
            return authTokenOptional;
        }
        return Optional.empty();
    }

    private boolean isValidPassword(UsernamePasswordAuthenticationToken authToken, String rawPassword) {
        return this.passwordEncoder
                .matches(rawPassword, ((User) authToken.getPrincipal()).getPassword());
    }

    private Boolean isOwnRepo(String uri, String username) { //TODO: check if user has rights to manage target repo
        String[] uriElements = Arrays.stream(uri.split("/"))
                .filter(element -> !element.isBlank())
                .toArray(String[]::new);

        return uriElements[1].equalsIgnoreCase(username);
    }
}
