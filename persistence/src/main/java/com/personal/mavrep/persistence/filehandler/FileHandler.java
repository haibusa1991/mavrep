package com.personal.mavrep.persistence.filehandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FileHandler {
    private final FileWriter fileSaver;
    private final FileReader fileReader;
    private final FileDeleter fileDeleter;


    public String saveFile(String filepath, byte[] content) throws WriteException {
        return this.fileSaver.saveFileToDisk(filepath, content);
    }

    public byte[] readFile(String uri, String localFilename) throws ReadException {

        return this.fileReader.readFile(uri, localFilename);
    }

    public void deleteFile(String path, String filename) throws DeleteException {
        this.fileDeleter.deleteFile(path,filename);
    }
}
