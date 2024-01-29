package com.personal.microart.api.operations.file.download;

import com.personal.microart.api.base.ProcessorInput;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class DownloadFileInput implements ProcessorInput {

    private String uri;
}
