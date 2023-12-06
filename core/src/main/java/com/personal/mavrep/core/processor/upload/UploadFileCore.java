package com.personal.mavrep.core.processor.upload;

import com.personal.mavrep.api.errors.*;
import com.personal.mavrep.api.operations.file.upload.UploadFileInput;
import com.personal.mavrep.api.operations.file.upload.UploadFileOperation;
import com.personal.mavrep.api.operations.file.upload.UploadFileResult;
import com.personal.mavrep.persistence.filehandler.FileHandler;
import com.personal.mavrep.persistence.repositories.ArtefactRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.ref.PhantomReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class UploadFileCore implements UploadFileOperation {
    private final FileHandler fileHandler;
    private final ArtefactRepository artefactRepository;


    @Override
    public Either<ApiError, UploadFileResult> process(UploadFileInput input) {
        this.validateFileExtension(input.getUri())
                .flatMap(this::validateFileVersion)
                .flatMap(uri -> this.saveFileToDisk(uri, input.getContent()))


//
//        if (this.isRedeploy(input.getUri())) {
//            return Either.left(new ArtefactRedeployError());
//        }
//
//        if (!this.isConformingFilename(input.getUri())) {
//            return Either.left(new InvalidFilenameException());
//        }
//
//
//        String filepath = input
//                .getUri()
//                .substring(0, input.getUri().lastIndexOf("/"))
//                //TODO: replace "/mvn/" hardcoded value with repo name
//                .replace("/mvn/", "");
//
//        String filenameOnDisk;
//        try {
//            filenameOnDisk = fileHandler.saveFile(filepath, input.getContent());
//        } catch (WriteException e) {
//            return Either.left(new FileUploadError());
//        }
//
//        String artefactName = filepath
//                .substring(0, filepath.lastIndexOf("/"))
//                .replace("/", ".");
//
//        String artefactVersion = filepath
//                .substring(filepath.lastIndexOf("/") + 1);
//
//        Optional<Artefact> artefactOptional = this.artefactRepository.findByUri(input.getUri());
//
//        if (artefactOptional.isPresent()) {
//            Artefact artefact = artefactOptional.get();
//            //TODO: replace "/mvn" with actual repo name
//            String path = artefact.getUri().substring(0, artefact.getUri().lastIndexOf("/")).replace("/mvn", "");
//
//            try {
//                this.fileHandler.deleteFile(path, artefact.getFilename());
//            } catch (DeleteException e) {
//                return Either.left(new FileUploadError());
//            }
//
//            artefact.setFilename(filenameOnDisk);
//            this.artefactRepository.save(artefact);
//            return Either.right(UploadFileResult.builder().build());
//        }
//
//        Artefact artefact = Artefact.builder()
//                .name(artefactName)
//                .version(artefactVersion)
//                .filename(filenameOnDisk)
//                .uri(input.getUri())
//                .build();
//
//        this.artefactRepository.save(artefact);

//        return Either.right(UploadFileResult.builder().build());
        return Either.left(ArtefactRedeployError.builder().build());
    }

    private Boolean isRedeploy(String uri) {
        // /mvn/com/personal/mrt/0.0.13-SNAPSHOT/mrt-0.0.13-20231122.203815-1.pom.md5
        Pattern pom_md5 = Pattern.compile(".*/[A-Za-z0-9]+-[A-Za-z0-9.-]+-\\d{8}\\.\\d{6}-\\d+\\.pom.md5$");

        // /mvn/com/personal/mrt/0.0.13-SNAPSHOT/mrt-0.0.13-20231122.203815-1.pom.sha1
        Pattern pom_sha1 = Pattern.compile(".*/[A-Za-z0-9]+-[A-Za-z0-9.-]+-\\d{8}\\.\\d{6}-\\d+\\.pom.sha1$");

        // /mvn/com/personal/mrt/0.0.13-SNAPSHOT/mrt-0.0.13-20231122.203815-1.pom
        Pattern pom = Pattern.compile(".*/[A-Za-z0-9]+-[A-Za-z0-9.-]+-\\d{8}\\.\\d{6}-\\d+\\.pom$");

        // /mvn/com/personal/mrt/0.0.13-SNAPSHOT/mrt-0.0.13-20231122.203815-1.jar.md5
        Pattern jar_md5 = Pattern.compile(".*/[A-Za-z0-9]+-[A-Za-z0-9.-]+-\\d{8}\\.\\d{6}-\\d+\\.jar.md5$");

        // /mvn/com/personal/mrt/0.0.13-SNAPSHOT/mrt-0.0.13-20231122.203815-1.jar.sha1
        Pattern jar_sha1 = Pattern.compile(".*/[A-Za-z0-9]+-[A-Za-z0-9.-]+-\\d{8}\\.\\d{6}-\\d+\\.jar.sha1$");

        // /mvn/com/personal/mrt/0.0.13-SNAPSHOT/mrt-0.0.13-20231122.203815-1.jar
        Pattern jar = Pattern.compile(".*/[A-Za-z0-9]+-[A-Za-z0-9.-]+-\\d{8}\\.\\d{6}-\\d+\\.jar$");

        return Stream.of(pom_md5, pom_sha1, pom,
                        jar_md5, jar_sha1, jar)
                .map(regex -> regex.matcher(uri))
                .map(matcher -> matcher.find() ? matcher.group() : "-1.")
                .map(string -> string.substring(string.lastIndexOf('-') + 1))
                .map(string -> string.substring(0, string.indexOf('.')))
                .mapToInt(Integer::parseInt)
                .anyMatch(value -> value != 1);
    }

    private Boolean isConformingFilename(String uri) {
        // /mvn/com/personal/mrt/0.0.13-SNAPSHOT/mrt-0.0.13-20231122.203815-1.pom.md5
        Pattern pom_md5 = Pattern.compile(".*/[A-Za-z0-9]+-[A-Za-z0-9.-]+-\\d{8}\\.\\d{6}-1\\.pom.md5$");

        // /mvn/com/personal/mrt/0.0.13-SNAPSHOT/mrt-0.0.13-20231122.203815-1.pom.sha1
        Pattern pom_sha1 = Pattern.compile(".*/[A-Za-z0-9]+-[A-Za-z0-9.-]+-\\d{8}\\.\\d{6}-1\\.pom.sha1$");

        // /mvn/com/personal/mrt/0.0.13-SNAPSHOT/mrt-0.0.13-20231122.203815-1.pom
        Pattern pom = Pattern.compile(".*/[A-Za-z0-9]+-[A-Za-z0-9.-]+-\\d{8}\\.\\d{6}-1\\.pom$");

        // /mvn/com/personal/mrt/0.0.13-SNAPSHOT/mrt-0.0.13-20231122.203815-1.jar.md5
        Pattern jar_md5 = Pattern.compile(".*/[A-Za-z0-9]+-[A-Za-z0-9.-]+-\\d{8}\\.\\d{6}-1\\.jar.md5$");

        // /mvn/com/personal/mrt/0.0.13-SNAPSHOT/mrt-0.0.13-20231122.203815-1.jar.sha1
        Pattern jar_sha1 = Pattern.compile(".*/[A-Za-z0-9]+-[A-Za-z0-9.-]+-\\d{8}\\.\\d{6}-1\\.jar.sha1$");

        // /mvn/com/personal/mrt/0.0.13-SNAPSHOT/mrt-0.0.13-20231122.203815-1.jar
        Pattern jar = Pattern.compile(".*/[A-Za-z0-9]+-[A-Za-z0-9.-]+-\\d{8}\\.\\d{6}-1\\.jar$");

        // /mvn/com/personal/mrt/0.0.1-SNAPSHOT/maven-metadata.xml.sha1
        Pattern maven_sha1 = Pattern.compile(".*/maven-metadata\\.xml\\.sha1$");

        // /mvn/com/personal/mrt/0.0.1-SNAPSHOT/maven-metadata.xml.md5
        Pattern maven_md5 = Pattern.compile(".*/maven-metadata\\.xml\\.md5$");

        // /mvn/com/personal/mrt/0.0.1-SNAPSHOT/maven-metadata.xml
        Pattern maven = Pattern.compile(".*/maven-metadata\\.xml$");

        return Stream.of(pom_md5, pom_sha1, pom,
                        jar_md5, jar_sha1, jar,
                        maven_md5, maven_sha1, maven)
                .map(regex -> regex.matcher(uri))
                .anyMatch(Matcher::matches);
    }

    private Either<ApiError, String> validateFileExtension(String uri) {
        String extension = uri.substring(uri.lastIndexOf("."));

        return Try.of(() -> {
                    FileExtension.valueOf(extension.trim().toUpperCase());
                    return uri;
                })
                .toEither()
                .mapLeft(throwable -> InvalidFileExtensionError.builder().build());

    }

    private Either<ApiError, String> validateFileVersion(String uri) {
        return Try.of(() -> {
                    Boolean isValid = Stream.of(Pattern.compile(".*\\d+\\.[A-Za-z0-9]{3}$"))
                            .map(regex -> regex.matcher(uri))
                            .map(matcher -> matcher.find() ? matcher.group() : "-1.")
                            .map(string -> string.substring(string.lastIndexOf('-') + 1))
                            .map(string -> string.substring(0, string.indexOf('.')))
                            .mapToInt(Integer::parseInt)
                            .anyMatch(value -> value != 1);


                    if (!isValid) {
                        throw new RuntimeException("artefact is redeploy");
                    }

                    return uri;
                })
                .toEither()
                .mapLeft(throwable -> ArtefactRedeployError.builder().build());
    }

    private Either<ApiError, String> saveFileToDisk(String uri, byte[] content) {
        return Try.of(() -> fileHandler.saveFile(uri.substring(0, uri.lastIndexOf("/")), content))
                .toEither()
                .mapLeft(throwable -> FileUploadError.builder().build());
    }

    private Either<ApiError, String> updateMetadata() {
        return null;
    }

    private Either<ApiError, String> updateDatabaseRecord() {
        return null;
    }


}
