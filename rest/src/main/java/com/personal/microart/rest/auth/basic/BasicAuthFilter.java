package com.personal.microart.rest.auth.basic;

import com.personal.microart.core.auth.basic.BasicAuthFilterCore;
import com.personal.microart.rest.auth.base.BaseFilter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasicAuthFilter extends BaseFilter {
    private final BasicAuthFilterCore filterCore;
    private final BasicAuthFilteredEndpoints endpoints;

    @PostConstruct
    private void init() {
        super.setFilterCore(this.filterCore);
        super.setEndpoints(this.endpoints);
    }
}
