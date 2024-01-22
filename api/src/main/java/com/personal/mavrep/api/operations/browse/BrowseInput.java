package com.personal.mavrep.api.operations.browse;

import com.personal.mavrep.api.base.ProcessorInput;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class BrowseInput implements ProcessorInput {

    private String uri;
}
