package com.personal.microart.api.operations.file.upload;

import com.personal.microart.api.base.ProcessorInput;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class UploadFileInput implements ProcessorInput {

    private byte[] content;

    @NotEmpty
    private String authentication;
    private String uri;

}
