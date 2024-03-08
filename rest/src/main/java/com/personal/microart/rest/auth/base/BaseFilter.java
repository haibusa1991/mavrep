package com.personal.microart.rest.auth.base;

import com.personal.microart.core.auth.base.FilterCore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseFilter extends OncePerRequestFilter {
    @Setter
    private FilterCore filterCore;
    @Setter
    private ProtectedEndpoints protectedEndpoints;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = this.filterCore.getAuthentication(request);
        boolean isAnonymousUser = authentication instanceof AnonymousAuthenticationToken;

        if (this.filterCore.isProtectedEndpoint(request, protectedEndpoints.getProtectedEndpoints()) && isAnonymousUser) {
            System.out.printf("%s denied access", this.getClass().getSimpleName()); //TODO: replace with logger
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(this.filterCore.getAuthentication(request));
        filterChain.doFilter(request, response);
    }
}
