package com.personal.microart.persistence.repositories;

import com.personal.microart.persistence.entities.MicroartUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<MicroartUser, UUID> {

    Optional<MicroartUser> findByEmail(String email);
    Optional<MicroartUser> findByUsername(String username);
}
