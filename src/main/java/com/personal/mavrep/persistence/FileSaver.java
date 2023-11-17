package com.personal.mavrep.persistence;

import com.personal.mavrep.exceptions.VersionAlreadyDeployedException;
import com.personal.mavrep.exceptions.storage.FileNotSavedException;
import io.vavr.API;
import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

@Component
public class FileSaver {
    @Value("${SAVE_LOCATION}")
    private String SAVE_LOCATION;

    public void saveFileToDisk(String fullFilepath, byte[] data) {
//        if (isExistingArtefact(fullFilepath)) {
//            String artefactId = fullFilepath
//                    .substring(5, fullFilepath.lastIndexOf('/'))
//                    .replace('/', '.');
//
//            throw new VersionAlreadyDeployedException(artefactId);
//        }

        this.createDirectoryStructure(fullFilepath);

        try (FileOutputStream fileOutputStream = new FileOutputStream(this.SAVE_LOCATION + fullFilepath)) {
            fileOutputStream.write(data);
        } catch (Exception e) {
            throw new FileNotSavedException();
        }
    }

    private Boolean isExistingArtefact(String fullFilepath) {
        String path = SAVE_LOCATION + fullFilepath.substring(0, fullFilepath.lastIndexOf('/'));

        return Files.exists(Path.of(path));
    }

    private Path createDirectoryStructure(String fullFilepath) {
        String[] artefactId = fullFilepath
                .substring(fullFilepath.indexOf('/'), fullFilepath.lastIndexOf('/'))
                .split("/");

        try {
            return Files.createDirectories(Path.of(this.SAVE_LOCATION, artefactId));
        } catch (IOException e) {
            throw new FileNotSavedException();
        }

    }
}
