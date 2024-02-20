package com.personal.microart.api.operations.browse;


import com.personal.microart.api.base.Processor;

/**
 * The BrowseOperation interface represents a browsing operation in the application. It lists all the public vaults and
 * artefacts and the vaults and artefacts that the current user is authorized to see. Provides link for artefact
 * download. Response is structured as an object that allows for easy frontend rendering.
 */
public interface BrowseOperation extends Processor<BrowseResult, BrowseInput> {

}
