package com.personal.mavrep.rest.persistence;

import com.personal.mavrep.rest.exceptions.storage.FileNotSavedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
