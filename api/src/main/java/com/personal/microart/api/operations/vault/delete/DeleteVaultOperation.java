package com.personal.microart.api.operations.vault.delete;


import com.personal.microart.api.base.Processor;
//TODO: Update documentation
/**
 * The CreateVaultOperation creates a new vault for the current user. The owner is added as an authorized user. Returns the following errors:
 * <ul>
 *      <li>{@link com.personal.microart.api.errors.DuplicateVaultNameError DuplicateVaultNameError} if a vault with the same name already exists</li>
 *      <li>{@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if the vault cannot be saved to database</li>
 * </ul>
 */
public interface DeleteVaultOperation extends Processor<DeleteVaultResult, DeleteVaultInput> {

}
