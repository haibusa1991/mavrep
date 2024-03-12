package com.personal.microart.rest.controllers;

import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.spec.HttpServletRequestImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Helper class that is responsible for accessing the {@link HttpServletRequestImpl} from HttpServletResponse that is
 * wrapped by Spring Security in a {@link org.springframework.security.web.firewall.FirewalledRequest FirewalledRequest}.
 * Required in order to set the reason phrase of the response.
 */
@Component
public class ExchangeAccessor {
    @SneakyThrows
    public HttpServerExchange getExchange(HttpServletResponse response) {
        Field requestField = response.getClass().getDeclaredField("request");
        requestField.setAccessible(true);

        ServletRequest servletRequest = ((ServletRequestWrapper) requestField.get(response)).getRequest();
        Method getRequestMethod = servletRequest.getClass().getMethod("getRequest");

        return ((HttpServletRequestImpl) getRequestMethod.invoke(servletRequest)).getExchange();
    }
}
