package com.personal.microart.api.operations.file.upload;


import com.personal.microart.api.base.Processor;
/**
 * Tries to upload a file to a given URI. File name is sanitized and changed to UUID. File contents are written to the
 * file system and a database record is created that represents the URI and the file name.
 *  Returns the following errors:
 *  <ul>
 *      <li>{@link com.personal.microart.api.errors.ConstraintViolationError ConstraintViolationError} if filename is invalid or version is already deployed</li>
 *      <li>{@link com.personal.microart.api.errors.InvalidCredentialsError InvalidCredentialsError} if the user is not authorized to upload</li>
 *      <li>{@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if the file could not be written to disk</li>
 *  </ul>
 */
public interface UploadFileOperation extends Processor<UploadFileResult, UploadFileInput> {

}
