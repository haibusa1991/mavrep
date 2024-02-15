package com.personal.microart.rest.auth;

import com.personal.microart.core.auth.filters.JwtAuthFilterCore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtAuthFilterCore filterCore;
    private final JwtWhitelistedEndpoints endpoints;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        Boolean isWhitelisted = this.filterCore.isWhitelisted(request, this.endpoints.getWhitelistedEndpoints());
//        Boolean hasValidCredentials = this.filterCore.hasValidCredentials(request);
////TODO: filter should reject request if target vault is private. Should permit browsing root directory and public vaults
//        if (!isWhitelisted && !hasValidCredentials) {
//            response.setContentType(MediaType.TEXT_HTML_VALUE);
//            response.setStatus(HttpStatus.FORBIDDEN.value());
//            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//            return;
//        }

        filterChain.doFilter(request, response);
    }
}
