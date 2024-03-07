package com.personal.microart.api.operations.user.logout;

import com.personal.microart.api.base.ProcessorInput;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class LogoutInput implements ProcessorInput {

    @Length(max = 300, message = "must be less than {max} characters")
    private String authentication;
}
