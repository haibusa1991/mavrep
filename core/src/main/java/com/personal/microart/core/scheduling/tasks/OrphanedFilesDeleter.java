package com.personal.microart.core.scheduling.tasks;

import com.personal.microart.core.scheduling.base.ScheduledTask;
import com.personal.microart.persistence.directorymanager.DefaultDirectoryManager;
import com.personal.microart.persistence.directorymanager.Directory;
import com.personal.microart.persistence.directorymanager.FileDeleter;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Deletes all files that are on disk but have no URI associated with them in the database at 02:00 every day.
 */
@Component
@RequiredArgsConstructor
public class OrphanedFilesDeleter implements ScheduledTask { //Grelod the Kind
    private final ArtefactRepository artefactRepository;
    private final FileDeleter fileDeleter;
    private final DefaultDirectoryManager directoryManager;

    @Override
    @Scheduled(cron = "0 0 2 * * *")
    public void runScheduledTask() {
        directoryManager.getAllFiles()
                .get()
                .stream()
                .map(this::findOrphans)
                .flatMap(List::stream)
                .forEach(this.fileDeleter::delete);
    }

    private List<String> findOrphans(Directory directory) {
        List<String> filesInDirectory = Arrays.stream(directory.getContent()).collect(Collectors.toList());

        filesInDirectory.removeAll(this.artefactRepository.findArtefactByFilenameStartingWith(directory.getName())
                .stream()
                .map(Artefact::getFilename)
                .map(name -> name.split("/")[1])
                .toList());

        return filesInDirectory
                .stream()
                .map(filename -> directory.getName() + "/" + filename)
                .toList();
    }
}
