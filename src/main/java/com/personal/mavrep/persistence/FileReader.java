package com.personal.mavrep.persistence;

import com.personal.mavrep.exceptions.storage.FileNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FileReader {
    @Value("${SAVE_LOCATION}")
    private String SAVE_LOCATION;


    public byte[] readFile(String uri) {
        if (!Files.exists(Path.of(SAVE_LOCATION + uri))) {
            throw new FileNotFoundException();
        }

        try (FileInputStream file = new FileInputStream(this.SAVE_LOCATION + "/" + uri)) {
            return file.readAllBytes();
        } catch (Exception e) {
            throw new FileNotFoundException();
        }
    }
}
