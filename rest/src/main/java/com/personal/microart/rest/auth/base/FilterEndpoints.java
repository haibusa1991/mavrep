package com.personal.microart.rest.auth.base;

import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

public interface FilterEndpoints {

    Map<HttpMethod, List<String>> getFilteredEndpoints();
}
