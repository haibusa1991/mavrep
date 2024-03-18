package com.personal.microart.api.operations.vault.adduser;

import com.personal.microart.api.base.ProcessorResult;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class AddUserResult implements ProcessorResult {
    private final String jwt;
}
