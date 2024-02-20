package com.personal.microart.rest.auth.jwt;

import com.personal.microart.core.auth.jwt.JwtAuthFilterCore;
import com.personal.microart.rest.auth.base.BaseFilter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends BaseFilter {
    private final JwtAuthFilterCore filterCore;
    private final JwtFilteredEndpoints endpoints;

    @PostConstruct
    private void init() {
        super.setFilterCore(this.filterCore);
        super.setEndpoints(this.endpoints);
    }
}
