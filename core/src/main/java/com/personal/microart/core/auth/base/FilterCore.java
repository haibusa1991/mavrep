package com.personal.microart.core.auth.base;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

/**
 * FilterCore interface provides methods for authentication and authorization.
 * It is used to check if a request is authenticated, authorized and filtered.
 */
public interface FilterCore {

    /**
     * Provides an Authentication object to use with the SecurityContextHolder. The Authentication object can be
     * an AnonymousAuthenticationToken, or an Authentication interface implementation.
     *
     * @param request The HTTP request from which to retrieve the authentication information.
     * @return The authentication information.
     */
    Authentication getAuthentication(HttpServletRequest request);


    /**
     * Checks if the filter should be applied to the provided HTTP request. Provided map specifies the HTTP methods
     * and their corresponding endpoints.
     *
     * @param request The HTTP request to check.
     * @param protectedEndpoints The map of HTTP methods and their corresponding protected endpoints.
     * @return True if the endpoint is protected, false otherwise.
     */
    Boolean isProtectedEndpoint(HttpServletRequest request, Map<HttpMethod, List<String>> protectedEndpoints);

}