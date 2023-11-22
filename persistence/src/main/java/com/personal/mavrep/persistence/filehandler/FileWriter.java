package com.personal.mavrep.persistence.filehandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class FileWriter {
    @Value("${SAVE_LOCATION}")
    private String SAVE_LOCATION;

    public String saveFileToDisk(String filepath, byte[] data) throws WriteException {

        this.createDirectoryStructure(filepath);
        String filename = this.getRandomFilename();

        try (FileOutputStream fileOutputStream = new FileOutputStream(this.SAVE_LOCATION + "/" +filepath + "/" + filename)) {
            fileOutputStream.write(data);
            return filename;
        } catch (Exception e) {
            throw new WriteException(filename);
        }
    }

    private Path createDirectoryStructure(String filepath) throws WriteException {

        try {
            return Files.createDirectories(Path.of(this.SAVE_LOCATION, filepath.split("/")));
        } catch (IOException e) {
            throw new WriteException(filepath);
        }
    }

    private String getRandomFilename() {
        return UUID.randomUUID().toString();
    }
}
