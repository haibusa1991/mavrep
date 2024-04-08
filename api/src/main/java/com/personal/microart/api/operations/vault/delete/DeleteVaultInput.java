package com.personal.microart.api.operations.vault.delete;

import com.personal.microart.api.base.ProcessorInput;
import com.personal.microart.validation.constraints.UrlSafe;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteVaultInput implements ProcessorInput {

    @UrlSafe
    @Length(max = 100)
    private String vaultName;
}
