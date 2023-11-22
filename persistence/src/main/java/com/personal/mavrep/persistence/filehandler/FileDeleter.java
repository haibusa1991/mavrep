package com.personal.mavrep.persistence.filehandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class FileDeleter {
    @Value("${SAVE_LOCATION}")
    private String SAVE_LOCATION;

    public void deleteFile (String path, String filename) throws DeleteException {

        File targetFile = new File(this.SAVE_LOCATION + "/" + path + "/" + filename);
        try {
            targetFile.delete();
        } catch (Exception e) {
            throw new DeleteException(filename);
        }
    }
}
