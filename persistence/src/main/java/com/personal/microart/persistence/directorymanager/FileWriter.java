package com.personal.microart.persistence.directorymanager;

import com.personal.microart.persistence.errors.PersistenceError;
import com.personal.microart.persistence.errors.WriteError;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.util.UUID;

/**
 * Writes a file to the file system and returns the relative path to the file, e.g. /01fe/ebf00a68-7903-4820-917a-f1ecfa3d418f
 * Correct directory is determined by the {@link DirectoryManager}.
 * Can return the following errors:
 * <ul>
 *     <li>{@link WriteError} if the file cannot be written</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class FileWriter {
    @Value("${SAVE_LOCATION}")
    private String SAVE_LOCATION;

    private final DefaultDirectoryManager directoryManager;

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
