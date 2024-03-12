package com.personal.microart.persistence.filehandler;

import com.personal.microart.persistence.errors.Error;
import com.personal.microart.persistence.errors.PersistenceError;
import com.personal.microart.persistence.errors.ReadError;
import com.personal.microart.persistence.errors.WriteError;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Default implementation of the {@link DirectoryManager}. Stores the files in hexadecimal numbered directories,
 * starting from 00. When the maximum directory size or file count is reached, a new directory is created.
 * Limits are set in the application properties. Current directory is stored in a cache to avoid unnecessary
 * file system operations. Cache is invalidated when a new directory is created.
 */
@Component
@RequiredArgsConstructor
public class DefaultDirectoryManager implements DirectoryManager {

    @Value("${SAVE_LOCATION}")
    private String SAVE_LOCATION;

    @Value("${MAXIMUM_DIRECTORY_SIZE_MB}")
    private Integer MAXIMUM_DIRECTORY_SIZE_MB;

    @Value("${MAXIMUM_DIRECTORY_ITEMS_COUNT}")
    private Integer MAXIMUM_DIRECTORY_ITEMS_COUNT;
    private final String CACHEABLE_NAME = "activeDirectory";

    private final CacheManager cacheManager;

    @Cacheable(CACHEABLE_NAME)
    @Override
    public Either<PersistenceError, String> getActiveDirectory() {
        return Try.withResources(() -> Files.list(Path.of(this.SAVE_LOCATION)))
                .of(stream -> {
                    Files.createDirectories(Path.of(this.SAVE_LOCATION + "/00"));

                    return stream.filter(Files::isDirectory)
                            .map(Path::getFileName)
                            .map(Path::toString)
                            .filter(this::isParsable)
                            .filter(directoryName -> directoryName.length() % 2 == 0) //filter out directories with odd number of chars - directory "1fe" is different from "01fe" but both are equal to 510 dec
                            .map(directoryName -> Integer.parseInt(directoryName, 16))
                            .sorted((i1, i2) -> Integer.compare(i2, i1))
                            .map(directoryName -> Integer.toString(directoryName, 16))
                            .map(directoryName -> directoryName.length() % 2 == 1 ? "0" + directoryName : directoryName) //pad directory names with zero if odd number of symbols
                            .findFirst()
                            .orElse("00");
                })
                .toEither()
                .mapLeft(throwable -> WriteError.builder().message(throwable.getMessage()).build());
    }

    private Boolean isParsable(String s) {
        try {
            Integer.valueOf(s, 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Either<PersistenceError, String> updateActiveDirectory(String currentActiveDirectory, String persistedFileName) {
        return Try
                .withResources(() -> Files.walk(Path.of(this.SAVE_LOCATION + "/" + currentActiveDirectory)))
                .of(paths ->
                {
                    List<Path> files = paths
                            .filter(path -> !Files.isDirectory(path))
                            .toList();

                    Integer fileCount = files.size();
                    Long fileSizeMb = files.stream().mapToLong(path -> path.toFile().length()).sum() / 1024 / 1024;

                    if (fileCount > this.MAXIMUM_DIRECTORY_ITEMS_COUNT - 1 || fileSizeMb > this.MAXIMUM_DIRECTORY_SIZE_MB) {
                        this.cacheManager.getCache(this.CACHEABLE_NAME).invalidate();

                        Integer currentNumber = Integer.parseInt(currentActiveDirectory, 16);
                        String newNumber = Integer.toString(++currentNumber, 16);
                        Files.createDirectories(Path.of(this.SAVE_LOCATION + "/" + (newNumber.length() % 2 == 0 ? newNumber : "0" + newNumber)));
                    }

                    return persistedFileName;
                })
                .toEither()
                .mapLeft(throwable -> WriteError.builder().message(throwable.getMessage()).build());
    }

    @Override
    public Either<PersistenceError, List<Directory>> getAllFiles() {
        return Try.withResources(() -> Files.list(Path.of(this.SAVE_LOCATION)))
                .of(files -> files.filter(Files::isDirectory)
                                .map(dir -> Directory.builder()
                                        .name(dir.getFileName().toString())
                                        .content(dir.toFile().list())
                                        .build())
                                .toList())
                .toEither()
                .mapLeft(throwable -> ReadError.builder().error(Error.READ_ERROR).build());
    }
}
