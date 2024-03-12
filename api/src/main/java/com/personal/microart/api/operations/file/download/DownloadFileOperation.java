package com.personal.microart.api.operations.file.download;


import com.personal.microart.api.base.Processor;

/**
 * Tries to download a file from a given URI. The file is read from the file system and returned as a byte array.
 *  Returns the following errors:
 *  <ul>
 *      <li>{@link com.personal.microart.api.errors.FileNotFoundError FileNotFoundError} if the file is not found or user is not authorized</li>
 *      <li>{@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if the file could not be read from disk</li>
 *  </ul>
 */
public interface DownloadFileOperation extends Processor<DownloadFileResult, DownloadFileInput> {

}
