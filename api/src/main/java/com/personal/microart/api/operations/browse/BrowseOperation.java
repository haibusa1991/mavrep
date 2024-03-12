package com.personal.microart.api.operations.browse;


import com.personal.microart.api.base.Processor;

/**
 * The BrowseOperation interface represents a browsing operation of a vault. It lists all the public vaults and
 * artefacts and the vaults and artefacts that the current user is authorized to see. Provides link for artefact
 * download. Response is structured as a {@link Content} object that allows for easy frontend rendering. Returns
 * {@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if
 * database is not available.
 */
public interface BrowseOperation extends Processor<BrowseResult, BrowseInput> {

}
