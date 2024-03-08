package com.personal.microart.rest.auth.base;

import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

public interface ProtectedEndpoints {

    Map<HttpMethod, List<String>> getProtectedEndpoints();
}
