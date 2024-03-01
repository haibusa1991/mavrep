package com.personal.microart.api.operations.verifypassordresettoken;

import com.personal.microart.api.base.ProcessorInput;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class VerifyPasswordResetTokenInput implements ProcessorInput {

    @Length(max = 100, message = "must be less than {max} characters")
    @NotNull
    private String resetToken;

}
