package com.personal.microart.api.operations.user.requestpassword;

import com.personal.microart.api.base.ProcessorInput;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class RequestPasswordInput implements ProcessorInput {

    @Email
    @NotNull
    @Length(max = 40)
    private String email;

}
