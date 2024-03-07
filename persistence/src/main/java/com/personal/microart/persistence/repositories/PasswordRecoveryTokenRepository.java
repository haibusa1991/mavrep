package com.personal.microart.persistence.repositories;

import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.PasswordRecoveryToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordRecoveryTokenRepository extends JpaRepository<PasswordRecoveryToken, UUID> {

    Optional<PasswordRecoveryToken> findByUserAndIsValidTrue(MicroartUser user);

    Optional<PasswordRecoveryToken> findByTokenValue(String value);

}
