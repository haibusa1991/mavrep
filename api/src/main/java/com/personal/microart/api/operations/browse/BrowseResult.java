package com.personal.microart.api.operations.browse;

import com.personal.microart.api.base.ProcessorResult;
import lombok.*;

import java.util.Set;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class BrowseResult implements ProcessorResult {

    private Set<Content> content;

}
