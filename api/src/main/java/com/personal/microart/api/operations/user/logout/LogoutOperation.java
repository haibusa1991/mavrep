package com.personal.microart.api.operations.user.logout;


import com.personal.microart.api.base.Processor;

/**
 * The LogoutOperation logs out the user. On logout the user's jwt is blacklisted. Operation always returns a 2xx code,
 * regardless whether the token was missing, already blacklisted, valid or invalid. Record of the token is added to the database only
 * if the token is valid.
 * Returns the following errors:
 * <ul>
 *     <li>{@link com.personal.microart.api.errors.SilentFailError SilentFailError} if the token is invalid</li>
 *     <li>{@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if the database is not available</li>
 */
public interface LogoutOperation extends Processor<LogoutResult, LogoutInput> {

}
