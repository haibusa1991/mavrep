package com.personal.mavrep.persistence.filehandler;

import com.personal.mavrep.persistence.errors.PersistenceError;
import com.personal.mavrep.persistence.errors.WriteError;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataWriter {
    @Value("${SAVE_LOCATION}")
    private String SAVE_LOCATION;

    private final DirectoryManager directoryManager;

    public Either<PersistenceError, String> saveFileToDisk(byte[] data) {
        return this.directoryManager.getActiveDirectory()
                .flatMap(activeDirectory -> this.writeFile(activeDirectory, data));

    }

    private Either<PersistenceError, String> writeFile(String activeDirectory, byte[] data) {
        String filename = UUID.randomUUID().toString();

        return Try.withResources(() -> new FileOutputStream(this.SAVE_LOCATION + "/" + activeDirectory + "/" + filename))
                .of(fileOutputStream -> {
                    fileOutputStream.write(data);

                    String persistedFileName = activeDirectory + "/" + filename;
                    this.directoryManager.updateActiveDirectory(activeDirectory, persistedFileName);

                    return persistedFileName;
                })
                .toEither()
                .mapLeft(throwable -> WriteError.builder().message(throwable.getMessage()).build());

    }


}
