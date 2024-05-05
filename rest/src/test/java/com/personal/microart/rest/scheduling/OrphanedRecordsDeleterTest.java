package com.personal.microart.rest.scheduling;

import com.personal.microart.core.scheduling.tasks.OrphanedFileRecordsDeleter;
import com.personal.microart.core.scheduling.tasks.OrphanedFilesDeleter;
import com.personal.microart.persistence.directorymanager.FileDeleter;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class OrphanedRecordsDeleterTest {

    @Autowired
    private OrphanedFileRecordsDeleter orphanedFileRecordsDeleter;

    @Autowired
    private ArtefactRepository artefactRepository;

    Artefact artefact1 = Artefact
            .builder()
            .filename("filename1")
            .uri("uri1")
            .build();

    Artefact artefact2 = Artefact
            .builder()
            .filename("filename2")
            .uri("uri2")
            .build();

    Artefact artefact3 = Artefact
            .builder()
            .filename(null)
            .uri("uri3")
            .build();

    Artefact artefact4 = Artefact
            .builder()
            .filename(null)
            .uri("uri4")
            .build();

    @BeforeEach
    public void setUp() {
        this.artefactRepository.saveAll(List.of(artefact1, artefact2, artefact3, artefact4));
    }

    public void tearDown() {
        this.artefactRepository.deleteAll();
    }

    @Test
    public void testRunScheduledTask() {
        this.orphanedFileRecordsDeleter.runScheduledTask();

        assertEquals(2, this.artefactRepository.count());
        assertTrue(this.artefactRepository.findAll().contains(artefact1));
        assertTrue(this.artefactRepository.findAll().contains(artefact2));
    }
}
