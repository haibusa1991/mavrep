package com.personal.microart.rest.auth.basic;

import com.personal.microart.core.auth.basic.BasicAuthFilterCore;
import com.personal.microart.rest.auth.base.BaseFilter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
/**
 * A filter that is responsible for authenticating requests using the Basic Authentication scheme, more specifically -
 * all uploads and downloads performed by Maven.
 */
@Component
@RequiredArgsConstructor
public class BasicAuthFilter extends BaseFilter {
    private final BasicAuthFilterCore filterCore;
    private final BasicAuthProtectedEndpoints endpoints;

    @PostConstruct
    private void init() {
        super.setFilterCore(this.filterCore);
        super.setProtectedEndpoints(this.endpoints);
    }
}
