package com.personal.microart.api.operations.vault.create;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.personal.microart.api.base.ProcessorInput;
import com.personal.microart.validation.constraints.UrlSafe;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateVaultInput implements ProcessorInput {

    @UrlSafe
    @Length(max = 100)
    private String vaultName;
}
