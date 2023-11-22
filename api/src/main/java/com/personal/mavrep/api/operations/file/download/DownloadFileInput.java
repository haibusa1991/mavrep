package com.personal.mavrep.api.operations.file.download;

import com.personal.mavrep.api.base.ProcessorInput;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class DownloadFileInput implements ProcessorInput {

    private String uri;
}
