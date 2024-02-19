package com.personal.microart.api.operations.browse;

import com.personal.microart.api.base.ProcessorInput;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class BrowseInput implements ProcessorInput {

    private String uri;
}
