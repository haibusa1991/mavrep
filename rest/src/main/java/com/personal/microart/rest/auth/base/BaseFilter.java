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

/**
 * Base class for filters that are used to protect endpoints. Each filter must provide its own {@link FilterCore} and
 * {@link ProtectedEndpoints} instances. If the request is for a protected endpoint and the user is not authenticated,
 * the filter will return a 403 Forbidden response. Otherwise, the filter will set the Authentication object in the
 * SecurityContextHolder and call the next filter in the chain.
 */
public abstract class BaseFilter extends OncePerRequestFilter {
    @Setter
    private FilterCore filterCore;
    @Setter
    private ProtectedEndpoints protectedEndpoints;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = this.filterCore.getAuthentication(request);

        Boolean isProtectedEndpoint = this.filterCore.isProtectedEndpoint(request, protectedEndpoints.getProtectedEndpoints());
        Boolean isAnonymousUser = authentication instanceof AnonymousAuthenticationToken;

        if (isProtectedEndpoint && isAnonymousUser) {
            System.out.printf("%s denied access", this.getClass().getSimpleName()); //TODO: replace with logger
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            return;
        }

//        SecurityContextHolder.getContext().setAuthentication(this.filterCore.getAuthentication(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
