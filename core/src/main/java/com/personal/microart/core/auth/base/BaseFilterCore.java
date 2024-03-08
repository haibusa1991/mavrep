package com.personal.microart.core.auth.base;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Map;
//TODO: revise the implementation of this class and all filters

/**
 * This abstract class provides a base implementation for a filter core. It is designed to be extended by all filter
 * cores.
 */
public abstract class BaseFilterCore implements FilterCore {
    /**
     * The application context of the current application. Each filter core should explicitly set this field.
     */
    @Setter
    private ApplicationContext context;

    /**
     * This method checks if the filter should be applied to a given HTTP request based on its method and URI.
     *
     * @param request           The HTTP request to check.
     * @param filteredEndpoints A map where the keys are HTTP methods and the values are lists of URIs.
     *                          If the request's method and URI match any of these, the method will return true.
     * @return True if the endpoint is protected, false otherwise.
     */
    public Boolean isProtectedEndpoint(HttpServletRequest request, Map<HttpMethod, List<String>> filteredEndpoints) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        return filteredEndpoints
                .get(HttpMethod.valueOf(method.toUpperCase()))
                .stream()
                .anyMatch(filteredUri -> context.getBean(AntPathMatcher.class).match(filteredUri, uri));
    }

    protected Authentication getCurrentToken() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}