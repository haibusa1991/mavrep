package com.personal.microart.api.operations.file.download;

import com.personal.microart.api.base.ProcessorInput;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class DownloadFileInput implements ProcessorInput {

    private String authentication;
    private String uri;
}
