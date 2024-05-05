package com.personal.microart.core.scheduling.tasks;

import com.personal.microart.core.scheduling.base.ScheduledTask;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Deletes all file records that have a URI, but no file associated with them from the database at 02:00 every day.
 */
@Component
@RequiredArgsConstructor
public class OrphanedFileRecordsDeleter implements ScheduledTask {
    private final ArtefactRepository artefactRepository;

    @Override
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void runScheduledTask() {
        this.artefactRepository.deleteAllByFilename(null);
    }
}
