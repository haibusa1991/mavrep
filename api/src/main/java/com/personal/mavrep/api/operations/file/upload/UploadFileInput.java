package com.personal.mavrep.api.operations.file.upload;

import com.personal.mavrep.api.base.ProcessorInput;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class UploadFileInput implements ProcessorInput {

    private byte[] content;
    private String uri;

}
