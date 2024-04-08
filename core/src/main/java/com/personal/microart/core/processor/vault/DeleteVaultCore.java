package com.personal.microart.core.processor.vault;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.operations.vault.delete.DeleteVaultInput;
import com.personal.microart.api.operations.vault.delete.DeleteVaultOperation;
import com.personal.microart.api.operations.vault.delete.DeleteVaultResult;
import io.vavr.control.Either;

public class DeleteVaultCore implements DeleteVaultOperation {
    @Override
    public Either<ApiError, DeleteVaultResult> process(DeleteVaultInput input) {
        return this.validateExisting(input)
                .flatMap(this::deleteArtefacts)
                .flatMap(this::deleteVault);
    }

    private Either<ApiError, String> validateExisting(DeleteVaultInput input) {
        return null;
    }


    private Either<ApiError, String> deleteArtefacts(String vaultName) {
        return null;
    }

    private Either<ApiError, DeleteVaultResult> deleteVault(String vaultName) {
        return null;
    }
}
