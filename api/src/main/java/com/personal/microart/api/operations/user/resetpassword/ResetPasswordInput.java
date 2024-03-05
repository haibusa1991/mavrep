package com.personal.microart.api.operations.user.resetpassword;

import com.personal.microart.api.base.ProcessorInput;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordInput implements ProcessorInput {


    @Length(min = 6, max = 40,message = "must be between {min} and {max} characters")
    @NotNull
    private String password;

    @Length(max = 100, message = "must be less than {max} characters")
    @NotNull
    private String resetToken;

}
