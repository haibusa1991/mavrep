package com.personal.microart.api.operations.file.download;

import com.personal.microart.api.base.ProcessorResult;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class DownloadFileResult implements ProcessorResult {

    private byte[] content;
    private String filename;

}
