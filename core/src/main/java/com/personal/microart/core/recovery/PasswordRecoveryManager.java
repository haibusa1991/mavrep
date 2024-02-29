package com.personal.microart.core.recovery;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.persistence.entities.MicroartUser;
import io.vavr.control.Either;

/**
 * The PasswordRecoveryManager is responsible for managing password recovery tokens. Each token is associated with a user.
 * The user cannot have more than one active token at a time. Getting a new token invalidates the old one. Tokens must
 * expire on use or after a certain period of time. Cleanup mechanism for expired tokens is required (e.g. a cron job).
 */
public interface PasswordRecoveryManager {
    /**
     * Generates a new recovery token for a given user in plain text format. The generated token must be
     * URL-safe so it can be sent to the user via email and used in a URL. The method takes a MicroartUser
     * as an argument and returns an Either object. The Either will contain an ApiError (e.g. ServiceUnavailableError)
     * if the token generation fails, or a plain text recovery token if the token generation is successful.
     *
     * @param user The MicroartUser object for whom the recovery token is to be generated. This should be a valid user object.
     * @return Either&lt;ApiError, String&gt If the token generation is successful, the Either will contain a plain text
     * recovery token. If the token generation fails, it will contain an ApiError with details about the error.
     */
    Either<ApiError, String> getRecoveryToken(MicroartUser user);
}
