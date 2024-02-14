package com.personal.microart.core.auth.filters;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


public interface FilterCore {
    Boolean hasValidCredentials(HttpServletRequest request);

    UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request);

}
