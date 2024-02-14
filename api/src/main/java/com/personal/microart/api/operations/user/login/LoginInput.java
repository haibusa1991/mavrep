package com.personal.microart.api.operations.user.login;

import com.personal.microart.api.base.ProcessorInput;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginInput implements ProcessorInput {

    @Email
    @NotNull
    @Length(max = 40)
    private String email;

    @Length(min = 6, max = 40)
    private String password;

}
