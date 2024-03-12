package com.personal.microart.core.auth.base;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

/**
 * FilterCore interface provides methods for checking whether a filter should be applied to a given HTTP request and
 * notifying the SecurityContextHolder of the authentication information.
 */
public interface FilterCore {

    /**
     * Provides an Authentication object to use with the SecurityContextHolder. The Authentication objects used in
     * the application are from the following types:
     * <ul>
     *     <li>{@link BasicAuthenticationToken}</li>
     *     <li>{@link JwtAuthenticationToken}</li>
     *     <li>{@link org.springframework.security.authentication.AnonymousAuthenticationToken AnonymousAuthenticationToken}</li>
     *     <li>Any other future implementation of the {@link org.springframework.security.core.Authentication Authentication} interface</li>
     * </ul>
     *
     * @param request The HTTP request from which to retrieve the authentication information.
     * @return The authentication information.
     */
    Authentication getAuthentication(HttpServletRequest request);


    /**
     * Checks whether the filter should be applied to the provided HTTP request. Provided map specifies the HTTP methods
     * and their corresponding endpoints.
     *
     * @param request The HTTP request to check.
     * @param protectedEndpoints The map of HTTP methods and their corresponding protected endpoints.
     * @return True if the endpoint is protected, false otherwise.
     */
    Boolean isProtectedEndpoint(HttpServletRequest request, Map<HttpMethod, List<String>> protectedEndpoints);

}