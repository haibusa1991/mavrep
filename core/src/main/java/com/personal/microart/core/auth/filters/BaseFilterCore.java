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

    public Boolean isFiltered(HttpServletRequest request, Map<HttpMethod, List<String>> filteredEndpoints) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        return filteredEndpoints
                .get(HttpMethod.valueOf(method.toUpperCase()))
                .stream()
                .anyMatch(filteredUri -> context.getBean(AntPathMatcher.class).match(filteredUri, uri));
    }
}
