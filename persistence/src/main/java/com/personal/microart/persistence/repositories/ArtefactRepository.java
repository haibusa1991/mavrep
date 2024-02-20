package com.personal.microart.persistence.repositories;

import com.personal.microart.persistence.entities.Artefact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ArtefactRepository extends JpaRepository<Artefact, UUID> {

    Optional<Artefact> findArtefactByUri(String uri);

    void deleteArtefactByUri(String uri);

    List<Artefact> findArtefactByFilenameStartingWith(String startsWith);

    Set<Artefact> findArtefactByUriStartingWith(String uri);

    Set<Artefact> findAllByFilename(String filename);

}
