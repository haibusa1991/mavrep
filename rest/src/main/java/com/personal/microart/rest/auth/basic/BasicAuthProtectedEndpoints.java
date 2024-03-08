package com.personal.microart.rest.auth.basic;

import com.personal.microart.rest.auth.base.ProtectedEndpoints;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class BasicAuthProtectedEndpoints implements ProtectedEndpoints {

    private final List<String> protectedGet = List.of("/mvn/**");
    private final List<String> protectedPost = List.of();
    private final List<String> protectedPut = List.of("/mvn/**");
    private final List<String> protectedDelete = List.of();
    private final List<String> protectedPatch = List.of();

    @Override
    public Map<HttpMethod, List<String>> getProtectedEndpoints() {
        return Map.of(
                HttpMethod.GET, protectedGet,
                HttpMethod.POST, protectedPost,
                HttpMethod.PUT, protectedPut,
                HttpMethod.DELETE, protectedDelete,
                HttpMethod.PATCH, protectedPatch
        );
    }
}
