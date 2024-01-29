package com.personal.microart.rest.auth;

import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Getter
public class WhitelistedEndpoints {

    private final List<String> whitelistedGet = List.of("/mvn/**");
    private final List<String> whitelistedPost = List.of("/user/**");
    private final List<String> whitelistedPut = List.of();
    private final List<String> whitelistedDelete = List.of();
    private final List<String> whitelistedPatch = List.of();

    public Map<HttpMethod, List<String>> whitelistedEndpoints = Map.of(
            HttpMethod.GET, whitelistedGet,
            HttpMethod.POST, whitelistedPost,
            HttpMethod.PUT, whitelistedPut,
            HttpMethod.DELETE, whitelistedDelete,
            HttpMethod.PATCH, whitelistedPatch
    );


}
