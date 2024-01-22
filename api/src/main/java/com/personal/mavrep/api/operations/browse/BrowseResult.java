package com.personal.mavrep.api.operations.browse;

import com.personal.mavrep.api.base.ProcessorResult;
import lombok.*;

import java.util.Set;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class BrowseResult implements ProcessorResult {

    private Set<Content> content;

}
