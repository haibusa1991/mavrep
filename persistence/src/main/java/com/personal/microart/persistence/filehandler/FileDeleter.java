package com.personal.microart.persistence.filehandler;

import com.personal.microart.persistence.errors.DeleteError;
import com.personal.microart.persistence.errors.PersistenceError;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Deletes a file from the file system based on the file name and relative path, e.g. /01fe/ebf00a68-7903-4820-917a-f1ecfa3d418f
 */
@Component
@RequiredArgsConstructor
public class FileDeleter {

    @Value("${SAVE_LOCATION}")
    private String SAVE_LOCATION;

    public Either<PersistenceError, Boolean> delete(String file) {
        return Try.of(() -> new File(this.SAVE_LOCATION + "/" + file).delete())
                .toEither()
                .mapLeft(throwable -> DeleteError.builder().build());
    }
}
