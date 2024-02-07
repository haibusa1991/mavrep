package com.personal.microart.rest.auth;

import com.personal.microart.core.auth.BasicAuthFilterCore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class BasicAuthFilter extends OncePerRequestFilter {
    private final BasicAuthFilterCore filterCore;
    private final ApplicationContext context;
    private final WhitelistedEndpoints endpoints;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Boolean isWhitelisted = this.isWhitelisted(request.getMethod(), request.getRequestURI());
        Boolean hasValidCredentials = this.filterCore.hasValidCredentials(request);

        if (!isWhitelisted && !hasValidCredentials) {
            response.setContentType("text/html");
            response.setStatus(403);
            response.setCharacterEncoding("UTF-8");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Boolean isWhitelisted(String method, String uri) {
        return this.endpoints
                .getWhitelistedEndpoints()
                .get(HttpMethod.valueOf(method.toUpperCase()))
                .stream()
                .anyMatch(whitelistedUri -> context.getBean(AntPathMatcher.class).match(whitelistedUri, uri));
    }

}
