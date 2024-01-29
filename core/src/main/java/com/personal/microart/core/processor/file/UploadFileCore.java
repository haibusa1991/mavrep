package com.personal.microart.core.processor.file;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.errors.FileUploadError;
import com.personal.microart.api.errors.ValidationError;
import com.personal.microart.api.operations.file.upload.UploadFileInput;
import com.personal.microart.api.operations.file.upload.UploadFileOperation;
import com.personal.microart.api.operations.file.upload.UploadFileResult;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.filehandler.FileWriter;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
@Transactional
public class UploadFileCore implements UploadFileOperation {
    private final FileWriter fileWriter;
    private final ArtefactRepository artefactRepository;

    @Override
    public Either<ApiError, UploadFileResult> process(UploadFileInput input) {

        return this.validateFilename(input)
                .flatMap(this::validatePermissions)
                .flatMap(this::writeFile)
                .flatMap(this::createFileRecord);
    }

    private Either<ApiError, UploadFileInput> validatePermissions(UploadFileInput input) {
        return Either.right(input); //TODO code me
    }

    private Either<ApiError, UploadFileInput> validateFilename(UploadFileInput input) {
        return Try.of(() -> {

                    String[] uriElements = input.getUri().split("/");
                    String filename = uriElements[uriElements.length - 1].toLowerCase();

                    Pattern typical = Pattern.compile("\\d{8}\\.\\d{6}-1\\..*");
                    Pattern javadoc = Pattern.compile("\\d{8}\\.\\d{6}-1-javadoc\\..*");
                    Pattern sources = Pattern.compile("\\d{8}\\.\\d{6}-1-sources\\..*");
                    Pattern metadata = Pattern.compile("maven-metadata\\.xml.*");

                    return Stream.of(typical, javadoc, sources, metadata)
                            .map(regex -> regex.matcher(filename))
                            .map(matcher -> matcher.find() ? matcher.group() : "")
                            .filter(result -> !result.isEmpty())
                            .findFirst()
                            .map(ignored -> input)
                            .orElseThrow(() -> new IllegalArgumentException("Version already deployed or filename is invalid."));

                })
                .toEither()
                .mapLeft(throwable -> ValidationError.builder().message(throwable.getMessage()).build());
    }

    private Either<ApiError, Tuple2<String, String>> writeFile(UploadFileInput input) {
        return this.fileWriter
                .saveFileToDisk(input.getContent())
                .map(filename -> Tuple.of(filename, input.getUri()))
                .mapLeft(persistenceError -> FileUploadError.builder().message(persistenceError.getMessage()).build());
    }

    private Either<ApiError, UploadFileResult> createFileRecord(Tuple2<String, String> filenameAndUri) {
        String filename = filenameAndUri._1;
        String uri = filenameAndUri._2;

        Artefact artefact = Artefact.builder()
                .filename(filename)
                .uri(uri)
                .build();


        return Try.of(() -> {
                    this.artefactRepository.deleteArtefactByUri(uri);
                    return this.artefactRepository.save(artefact);
                })
                .toEither()
                .map(ignored -> UploadFileResult.builder().build())
                .mapLeft(throwable -> FileUploadError.builder().build());
    }
}
