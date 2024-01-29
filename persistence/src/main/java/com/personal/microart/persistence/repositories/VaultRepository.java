package com.personal.microart.persistence.repositories;

import com.personal.microart.persistence.entities.Vault;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VaultRepository extends JpaRepository<Vault, UUID> {

    Optional<Vault> findVaultByName(String name);
}
