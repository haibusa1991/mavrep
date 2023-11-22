package com.personal.mavrep.persistence.filehandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FileReader {
    @Value("${SAVE_LOCATION}")
    private String SAVE_LOCATION;

    public byte[] readFile(String uri, String localFilename) throws ReadException {
//        TODO: replace hardcoded "/mvn/" with repo name
        String target = SAVE_LOCATION + "/" + uri.substring(0, uri.lastIndexOf("/")).replace("/mvn/", "") + "/" + localFilename;
        try (FileInputStream file = new FileInputStream(target)) {
            return file.readAllBytes();
        } catch (Exception e) {
            throw new ReadException(target);
        }
    }
}
