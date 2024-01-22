package com.personal.mavrep.core.processor.download;

import com.personal.mavrep.api.errors.ApiError;
import com.personal.mavrep.api.errors.FileNotFoundError;
import com.personal.mavrep.api.errors.ServiceUnavailableError;
import com.personal.mavrep.api.operations.file.download.DownloadFileInput;
import com.personal.mavrep.api.operations.file.download.DownloadFileOperation;
import com.personal.mavrep.api.operations.file.download.DownloadFileResult;
import com.personal.mavrep.persistence.errors.Error;
import com.personal.mavrep.persistence.filehandler.FileReader;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static io.vavr.API.*;


@RequiredArgsConstructor
@Component
public class DownloadFileCore implements DownloadFileOperation {
    private final FileReader fileReader;

    @Override
    public Either<ApiError, DownloadFileResult> process(DownloadFileInput input) {

        return this.fileReader.readFile(input.getUri())
                .map(content -> DownloadFileResult.builder().content(content).build())
                .mapLeft(error -> Match(error.getError()).of(
                        Case($(Error.READ_ERROR), readError -> ServiceUnavailableError.builder().build()),
                        Case($(Error.FILE_NOT_FOUND_ERROR), readError -> FileNotFoundError.builder().build())));

    }
}
