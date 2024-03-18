package com.personal.microart.api.operations.vault.adduser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.personal.microart.api.base.ProcessorInput;
import com.personal.microart.validation.constraints.Rfc5322Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddUserInput implements ProcessorInput {

    @NotEmpty
    @JsonIgnore
    @Length(max = 100)
    private String vaultName;


    @NotEmpty
    @Length(max = 40)
    private String username;

}
