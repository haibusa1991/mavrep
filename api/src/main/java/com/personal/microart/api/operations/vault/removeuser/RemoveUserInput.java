package com.personal.microart.api.operations.vault.removeuser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.personal.microart.api.base.ProcessorInput;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemoveUserInput implements ProcessorInput {

    @NotEmpty
    @JsonIgnore
    @Length(max = 100)
    private String vaultName;


    @NotEmpty
    @Length(max = 40)
    private String username;

}
