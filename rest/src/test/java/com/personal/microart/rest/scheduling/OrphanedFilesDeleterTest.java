package com.personal.microart.rest.scheduling;

import com.personal.microart.core.scheduling.tasks.OrphanedFilesDeleter;
import com.personal.microart.persistence.directorymanager.Directory;
import com.personal.microart.persistence.directorymanager.DirectoryManager;
import com.personal.microart.persistence.directorymanager.FileDeleter;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class OrphanedFilesDeleterTest {

    @Autowired
    private OrphanedFilesDeleter orphanedFilesDeleter;

    @Autowired
    private ArtefactRepository artefactRepository;

    @SpyBean
    private FileDeleter fileDeleter;

    private Directory dir1 = new Directory("dir1", new String[]{"file1", "file2"});
    private Directory dir2 = new Directory("dir2", new String[]{"file1", "file2"});

    private Artefact artefact1 = new Artefact("testUser/testRepo/file1", "dir1/file1");
    private Artefact artefact2 = new Artefact("testUser/testRepo/file2", "dir1/file2");

    @SpyBean
    private DirectoryManager directoryManager;

    @BeforeEach
    public void setUp() {
        this.artefactRepository.saveAll(List.of(artefact1, artefact2));
        when(directoryManager.getAllFiles()).thenReturn(Either.right(List.of(dir1, dir2)));
        when(fileDeleter.delete(anyString())).thenReturn(Either.right(true));
    }

    public void tearDown() {
        this.artefactRepository.deleteAll();
    }

    @Test
    public void testRunScheduledTask() {
        this.orphanedFilesDeleter.runScheduledTask();
        verify(this.fileDeleter, times(2)).delete(anyString());
    }
}
