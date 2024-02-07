package com.personal.microart.core.auth;

import com.personal.microart.persistence.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class BasicAuthFilterCore {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BasicAuthConverter basicAuthConverter;

    public Boolean hasValidCredentials(HttpServletRequest request) {

        BasicAuth auth = this.basicAuthConverter.getBasicAuth(request.getHeader("Authorization"));

        return this.userRepository
                .findByUsername(auth.getUsername())
                .map(microartUser -> this.passwordEncoder.matches(auth.getPassword(), microartUser.getPassword()))
                .orElse(false);
    }

//    private Boolean isOwnRepo(String uri, String username) { //TODO: check if user has rights to manage target repo
//        String[] uriElements = Arrays.stream(uri.split("/"))
//                .filter(element -> !element.isBlank())
//                .toArray(String[]::new);
//
//        return uriElements[1].equalsIgnoreCase(username);
//    }
}
