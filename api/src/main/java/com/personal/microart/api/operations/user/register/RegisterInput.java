package com.personal.microart.api.operations.user.register;

import com.personal.microart.api.base.ProcessorInput;
import com.personal.microart.validation.constraints.Rfc5322Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterInput implements ProcessorInput {

    @Rfc5322Email
    @NotEmpty
    @Length(max = 40)
    private String email;

    @NotEmpty
    @Pattern(regexp = "^[^:]*$",message = "cannot contain ':'")
    @Length(min = 1, max = 40)
    private String username;

    @Length(min = 6, max = 40)
    private String password;

}
