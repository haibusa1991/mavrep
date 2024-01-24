package com.personal.mavrep.api.operations.user.login;

import com.personal.mavrep.api.base.ProcessorResult;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class LoginResult implements ProcessorResult {
    //TODO: returns jwt as header
}
