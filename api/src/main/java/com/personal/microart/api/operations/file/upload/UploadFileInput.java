package com.personal.microart.api.operations.file.upload;

import com.personal.microart.api.base.ProcessorInput;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class UploadFileInput implements ProcessorInput {

    private byte[] content;
    private String uri;

}
