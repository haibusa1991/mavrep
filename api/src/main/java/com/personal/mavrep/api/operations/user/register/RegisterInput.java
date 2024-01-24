package com.personal.mavrep.api.operations.user.register;

import com.personal.mavrep.api.base.ProcessorInput;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterInput implements ProcessorInput {

    @Email
    @NotEmpty
    @Length(max = 40)
    private String email;

    @Length(min = 6, max = 40)
    private String password;

}
