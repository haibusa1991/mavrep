//package com.personal.mavrep.core.processor.download;
//
//import com.personal.mavrep.api.errors.ApiError;
//import com.personal.mavrep.api.errors.FileNotFoundError;
//import com.personal.mavrep.api.operations.file.download.DownloadFileInput;
//import com.personal.mavrep.api.operations.file.download.DownloadFileOperation;
//import com.personal.mavrep.api.operations.file.download.DownloadFileResult;
//import com.personal.mavrep.persistence.entities.Artefact;
//import com.personal.mavrep.persistence.filehandler.FileHandler;
//import com.personal.mavrep.persistence.repositories.ArtefactRepository;
//import io.vavr.control.Either;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//import java.util.Optional;
//
//@RequiredArgsConstructor
//@Component
//public class DownloadFileCore implements DownloadFileOperation {
//    private final ArtefactRepository artefactRepository;
//    private final FileHandler fileHandler;
//
//    @Override
//    public Either<ApiError, DownloadFileResult> process(DownloadFileInput input) {
//        Optional<Artefact> artefactOptional = this.artefactRepository.findByUri(input.getUri());
//
//        if (artefactOptional.isEmpty()) {
//            return Either.left(new FileNotFoundError());
//        }
//
//        Artefact artefact = artefactOptional.get();
//
//        byte[] content;
//        try {
//            content = this.fileHandler.readFile(artefact.getUri(), artefact.getFilename());
//        } catch (ReadException e) {
//            return Either.left(new FileNotFoundError());
//        }
//
//        DownloadFileResult result = DownloadFileResult
//                .builder()
//                .content(content)
//                .build();
//
//        return Either.right(result);
//    }
//}
