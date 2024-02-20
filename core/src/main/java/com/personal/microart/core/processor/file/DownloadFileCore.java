package com.personal.microart.core.processor.file;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.FileNotFoundError;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.operations.file.download.DownloadFileInput;
import com.personal.microart.api.operations.file.download.DownloadFileOperation;
import com.personal.microart.api.operations.file.download.DownloadFileResult;
import com.personal.microart.persistence.errors.Error;
import com.personal.microart.persistence.filehandler.FileReader;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static io.vavr.API.*;

/**
 * This component is responsible for file download operations. Gets the requested file uri and delegates to the file reader
 * to do the actual reading from the file system. Returns 404 if the file is not found or user is not authorized;
 * 503 if the file could not be read from disk.
 */
@RequiredArgsConstructor
@Component
public class DownloadFileCore implements DownloadFileOperation {
    private final FileReader fileReader;

    @Override
    public Either<ApiError, DownloadFileResult> process(DownloadFileInput input) {

        return this.fileReader.readFile(input.getUri())
                .map(content -> {
                    String[] uriElements = input.getUri().split("/");
                    String filename = uriElements[uriElements.length - 1];

                    return DownloadFileResult.builder().content(content).filename(filename).build();
                })
                .mapLeft(error -> Match(error.getError()).of(
                        Case($(Error.READ_ERROR), readError -> ServiceUnavailableError.builder().build()),
                        Case($(Error.FILE_NOT_FOUND_ERROR), readError -> FileNotFoundError.builder().build())));
    }
}
