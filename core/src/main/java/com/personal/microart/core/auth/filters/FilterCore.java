package com.personal.microart.core.auth.filters;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;


public interface FilterCore {
//    Boolean hasValidCredentials(HttpServletRequest request);

    Authentication getAuthentication(HttpServletRequest request);

    Boolean isAuthorized(HttpServletRequest request);

}
