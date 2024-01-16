package com.personal.mavrep.core.processor.upload;

import com.personal.mavrep.api.errors.ApiError;
import com.personal.mavrep.api.errors.FileUploadError;
import com.personal.mavrep.api.errors.ValidationError;
import com.personal.mavrep.api.operations.file.upload.UploadFileInput;
import com.personal.mavrep.api.operations.file.upload.UploadFileOperation;
import com.personal.mavrep.api.operations.file.upload.UploadFileResult;
import com.personal.mavrep.persistence.entities.Artefact;
import com.personal.mavrep.persistence.filehandler.DataWriter;
import com.personal.mavrep.persistence.repositories.ArtefactRepository;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UploadFileCore implements UploadFileOperation {
    private final DataWriter dataWriter;
    private final ArtefactRepository artefactRepository;

    @Override
    public Either<ApiError, UploadFileResult> process(UploadFileInput input) {

        return this.validateFilename(input)
                .flatMap(this::writeFile)
                .flatMap(this::createFileRecord);
    }

    private Either<ApiError, UploadFileInput> validateFilename(UploadFileInput input) {
        return Try.of(() -> {

                    String[] uriElements = input.getUri().split("/");
                    String filename = uriElements[uriElements.length - 1];
                    //TODO: the uri bellow is marked as invalid - '-1-sources' and '-1-javadoc' should be considered valid
                    // /mvn/my-test-repo/com/personal/mrt/0.0/16/20240116.183252-1/mrt-0.0/16/20240116.183252-1-sources.jar
                    String[] filenameElements = filename.split("-");
                    Integer firstDot = filenameElements[filenameElements.length - 1].indexOf('.');
                    String uploadCount = filenameElements[filenameElements.length - 1].substring(0, firstDot);

                    if (filename.toLowerCase().contains("maven-metadata.xml") || Integer.parseInt(uploadCount) == 1) {
                        return input;
                    }

                    throw new IllegalArgumentException("Version already deployed or filename is invalid.");
                })
                .toEither()
                .mapLeft(throwable -> ValidationError.builder().message(throwable.getMessage()).build());
    }

    private Either<ApiError, Tuple2<String, String>> writeFile(UploadFileInput input) {
        return this.dataWriter
                .saveFileToDisk(input.getContent())
                .map(filename -> Tuple.of(filename, input.getUri()))
                .mapLeft(persistenceError -> FileUploadError.builder().message(persistenceError.getMessage()).build());
    }

    private Either<ApiError, UploadFileResult> createFileRecord(Tuple2<String, String> filenameAndUri) {

        Artefact artefact = Artefact.builder()
                .filename(filenameAndUri._1)
                .uri(filenameAndUri._2)
                .build();

        return Try.of(() -> this.artefactRepository.save(artefact))
                .toEither()
                .map(x -> UploadFileResult.builder().build())
                .mapLeft(throwable -> FileUploadError.builder().build());
    }
}
