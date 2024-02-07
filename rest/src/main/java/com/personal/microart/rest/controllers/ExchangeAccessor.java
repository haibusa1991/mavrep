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

@Component
public class ExchangeUnwrapper {
    @SneakyThrows
    public HttpServerExchange getExchange(HttpServletResponse response) {
        Field field = response.getClass().getDeclaredField("request");
        field.setAccessible(true);

        ServletRequest request = ((ServletRequestWrapper) field.get(response)).getRequest();
        Method method = request.getClass().getMethod("getRequest");

        return ((HttpServletRequestImpl) method.invoke(request)).getExchange();
    }
}
