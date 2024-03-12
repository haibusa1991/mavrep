package com.personal.microart.rest.auth.base;

import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

/**
 * Represents the endpoints that are protected by the filter.
 */
public interface ProtectedEndpoints {
    /**
     * Returns a map of the protected endpoints and their respective HTTP methods. Values are Ant matcher patterns.
     * A valid example of a protected
     * endpoints map is:
     * <pre>
     * {@code
     *
     * Map<HttpMethod, List<String>> protectedEndpoints = new HashMap<>() {{
     *     put(HttpMethod.GET, List.of("/api/v1/**"));
     *     put(HttpMethod.POST, List.of("/api/v1/**","/api/v2/**"));
     *     put(HttpMethod.PUT, List.of("/api/v1/protected/endpoint"));
     *     put(HttpMethod.DELETE, List.of("/api/v1/protected/endpoint"));
     *     put(HttpMethod.PATCH, List.of("/api/v1/protected/endpoint"));
     * }}
     * }
     * </pre>
     */

    Map<HttpMethod, List<String>> getProtectedEndpoints();
}
