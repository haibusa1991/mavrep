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

        if (this.isWhitelisted(request.getMethod(), request.getRequestURI())) { //whitelisted = filter does not apply
            filterChain.doFilter(request, response);
            return;
        }

//        Optional<UsernamePasswordAuthenticationToken> securityToken = this.filterCore.getSecurityToken(request);

//        if (securityToken.isPresent()) {
//            SecurityContextHolder.getContext().setAuthentication(securityToken.get());
//            filterChain.doFilter(request, response);
//            return;
//        }
        if (this.filterCore.hasValidCredentials(request)) {
//            SecurityContextHolder.getContext().setAuthentication(securityToken.get());
            filterChain.doFilter(request, response);
            return;
        }

        response.setContentType("text/html");
        response.setStatus(403);
        response.setCharacterEncoding("UTF-8");
    }

    private Boolean isWhitelisted(String method, String uri) {
        return this.endpoints
                .getWhitelistedEndpoints()
                .get(HttpMethod.valueOf(method.toUpperCase()))
                .stream()
                .anyMatch(whitelistedUri -> context.getBean(AntPathMatcher.class).match(whitelistedUri, uri));
    }

}
