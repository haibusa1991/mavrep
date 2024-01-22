package com.personal.mavrep.persistence.filehandler;

import com.personal.mavrep.persistence.entities.Artefact;
import com.personal.mavrep.persistence.repositories.ArtefactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrphanManager implements CommandLineRunner {

    private final ArtefactRepository artefactRepository;
    private final FileDeleter fileDeleter;
    private final DirectoryManager directoryManager;

    @Override
    public void run(String... args) {
        this.deleteOrphanedFiles();
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void deleteOrphanedFiles() {
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
