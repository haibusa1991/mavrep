package com.personal.microart.core.recovery;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.persistence.entities.MicroartUser;
import io.vavr.control.Either;

/**
 * The PasswordRecoveryManager is responsible for managing password recovery tokens. Each token must be associated with a user.
 * The user cannot have more than one active token at a time. Getting a new token invalidates the old one. Tokens must
 * have an expiration time
 */
public interface PasswordRecoveryManager {
    /**
     * Generates a new URL-safe recovery token for a given user in plain text format.
     * Returns the following errors:
     * <ul>
     *     <li>{@link com.personal.microart.api.errors.ServiceUnavailableError ServiceUnavailableError} if the database is not available</li>
     * </ul>
     */
    Either<ApiError, String> getRecoveryToken(MicroartUser user);
}
