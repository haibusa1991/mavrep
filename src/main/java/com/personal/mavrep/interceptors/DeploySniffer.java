package com.personal.mavrep.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class DeploySniffer implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        System.out.printf("%s method for URL: %s%n", request.getMethod(), request.getRequestURI());
        return true;
    }

}
