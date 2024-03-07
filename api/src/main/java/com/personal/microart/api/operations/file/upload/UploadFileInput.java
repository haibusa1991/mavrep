package com.personal.microart.api.operations.file.upload;

import com.personal.microart.api.base.ProcessorInput;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class UploadFileInput implements ProcessorInput {

    //TODO: Add validation
    private byte[] content;
    private String authentication;
    private String uri;

}
