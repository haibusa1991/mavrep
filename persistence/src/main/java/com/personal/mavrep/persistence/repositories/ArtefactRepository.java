package com.personal.mavrep.persistence.repositories;

import com.personal.mavrep.persistence.entities.Artefact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ArtefactRepository extends JpaRepository<Artefact, UUID> {

    Optional<Artefact> findByUri(String uri);

}
