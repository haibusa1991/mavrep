package com.personal.microart.api.operations.user.requestpassword;

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
public class RequestPasswordInput implements ProcessorInput {

    @Rfc5322Email
    @NotNull
    @Length(max = 40, message = "must be less than {max} characters")
    private String email;

}
