package com.personal.microart.persistence.repositories;

import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.entities.BlacklistedJwt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface BlacklistedJwtRepository extends JpaRepository<BlacklistedJwt, UUID> {
    Boolean existsByToken(String jwt);
}
