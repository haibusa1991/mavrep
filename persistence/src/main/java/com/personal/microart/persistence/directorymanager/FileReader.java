package com.personal.microart.persistence.directorymanager;

import com.personal.microart.persistence.errors.Error;
import com.personal.microart.persistence.errors.PersistenceError;
import com.personal.microart.persistence.errors.ReadError;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

/**
 * Reads a file from the file system based on the file name and relative path, e.g. /01fe/ebf00a68-7903-4820-917a-f1ecfa3d418f
 * and returns the file content as a byte array. Can return the following errors:
 * <ul>
 *     <li>{@link Error#FILE_NOT_FOUND_ERROR} if the file is not found</li>
 *     <li>{@link Error#READ_ERROR} if the file cannot be read</li>
 * </ul>

 */
@Component
@RequiredArgsConstructor
public class FileReader {
    private final ArtefactRepository artefactRepository;

    @Value("${SAVE_LOCATION}")
    private String SAVE_LOCATION;

    public Either<PersistenceError, byte[]> readFile(String uri) {
        return this.getFilename(uri)
                .flatMap(this::readFileContent);

    }

    private Either<PersistenceError, String> getFilename(String uri) {
        return Try.of(() -> this.artefactRepository
                        .findArtefactByUri(uri).orElseThrow(IllegalArgumentException::new) // uri points to non-existent file
                        .getFilename())
                .toEither()
                .mapLeft(throwable -> Match(throwable).of(
                        Case($(instanceOf(IllegalArgumentException.class)), ignored -> ReadError.builder().error(Error.FILE_NOT_FOUND_ERROR).build()),
                        Case($(), ignored -> ReadError.builder().error(Error.READ_ERROR).build())
                ));
    }

    private Either<PersistenceError, byte[]> readFileContent(String filename) {

        return Try.withResources(() -> new FileInputStream(this.SAVE_LOCATION + "/" + filename))
                .of(FileInputStream::readAllBytes)
                .toEither()
                .mapLeft(throwable -> ReadError.builder().error(Error.READ_ERROR).build());
    }

}
