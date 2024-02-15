package com.personal.microart.rest.auth;

import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class BasicAuthFilteredEndpoints {

    private final List<String> filteredGet = List.of("/mvn/**");
    private final List<String> filteredPost = List.of();
    private final List<String> filteredPut = List.of("/mvn/**");
    private final List<String> filteredDelete = List.of();
    private final List<String> filteredPatch = List.of();

    @Getter
    private final Map<HttpMethod, List<String>> filteredEndpoints = Map.of(
            HttpMethod.GET, filteredGet,
            HttpMethod.POST, filteredPost,
            HttpMethod.PUT, filteredPut,
            HttpMethod.DELETE, filteredDelete,
            HttpMethod.PATCH, filteredPatch
    );
}
