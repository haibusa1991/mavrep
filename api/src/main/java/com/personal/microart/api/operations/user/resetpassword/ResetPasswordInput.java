package com.personal.microart.api.operations.user.resetpassword;

import com.personal.microart.api.base.ProcessorInput;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class ResetPasswordInput implements ProcessorInput {


    @Length(min = 6, max = 400)
    private String password;

    @Length(max = 100)
    @NotNull
    private String resetToken;

}
