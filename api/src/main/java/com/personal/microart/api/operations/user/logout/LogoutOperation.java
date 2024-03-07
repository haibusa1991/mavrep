package com.personal.microart.api.operations.user.logout;


import com.personal.microart.api.base.Processor;

/**
 * The LogoutOperation logs out the user. Blacklists the user's jwt and returns 204 if the request is successfully
 * processed, 503 if the token could not be blacklisted.
 */
public interface LogoutOperation extends Processor<LogoutResult, LogoutInput> {

}
