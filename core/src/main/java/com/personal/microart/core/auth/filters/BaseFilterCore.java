package com.personal.microart.core.auth.filters;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Map;


public abstract class BaseFilterCore implements FilterCore {
    @Setter
    private ApplicationContext context;

    public Boolean isWhitelisted(HttpServletRequest request, Map<HttpMethod, List<String>> whitelistedEndpoints) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        return whitelistedEndpoints
                .get(HttpMethod.valueOf(method.toUpperCase()))
                .stream()
                .anyMatch(whitelistedUri -> context.getBean(AntPathMatcher.class).match(whitelistedUri, uri));
    }
}
