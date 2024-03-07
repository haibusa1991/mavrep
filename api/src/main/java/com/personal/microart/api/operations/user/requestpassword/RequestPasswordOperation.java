package com.personal.microart.api.operations.user.requestpassword;

import com.personal.microart.api.base.Processor;

/**
 * Handles requests for password reset. User should provide email address to receive a password reset link.
 * If the email address is found and user is not disabled, a single use reset token is generated and sent to the user.
 * The token is saved to db and its expiration is monitored. Expired token means that the token has been used and/or
 * validity has expired. A token is valid for 30 minutes by default. A scheduled job is responsible for cleaning up expired tokens.
 */

//TODO: Which scheduler? Complete the documentation.
public interface RequestPasswordOperation extends Processor<RequestPasswordResult, RequestPasswordInput> {

}