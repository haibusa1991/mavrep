package com.personal.microart.api.operations.user.login;

import com.personal.microart.api.base.ProcessorInput;
import com.personal.microart.validation.constraints.Rfc5322Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginInput implements ProcessorInput {

    @Rfc5322Email
    @NotNull
    @Length(max = 40)
    private String email;

    @Length(min = 6, max = 40)
    private String password;

}
