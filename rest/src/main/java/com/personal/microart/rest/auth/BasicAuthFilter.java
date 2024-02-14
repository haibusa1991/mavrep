package com.personal.microart.rest.auth;

import com.personal.microart.core.auth.filters.BasicAuthFilterCore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class BasicAuthFilter extends OncePerRequestFilter {
    private final BasicAuthFilterCore filterCore;
    private final BasicAuthWhitelistedEndpoints endpoints;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Boolean isWhitelisted = filterCore.isWhitelisted(request, this.endpoints.getWhitelistedEndpoints());
        Boolean hasValidCredentials = this.filterCore.hasValidCredentials(request);

        if (!isWhitelisted && !hasValidCredentials) {
            System.out.println("BasicAuthFilter denied access"); //TODO: replace with logger
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            return;
        }

        if (!isWhitelisted) {
            SecurityContextHolder.getContext().setAuthentication(this.filterCore.getAuthentication(request));
            System.out.println();
        }

        filterChain.doFilter(request, response);
    }
}
