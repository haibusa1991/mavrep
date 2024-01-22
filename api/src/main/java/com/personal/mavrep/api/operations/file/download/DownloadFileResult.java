package com.personal.mavrep.api.operations.file.download;

import com.personal.mavrep.api.base.ProcessorResult;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class DownloadFileResult implements ProcessorResult {

    private byte[] content;

}
