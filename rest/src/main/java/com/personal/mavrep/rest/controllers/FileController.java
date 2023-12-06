package com.personal.mavrep.rest.controllers;

import com.personal.mavrep.api.errors.BaseApiError;
import com.personal.mavrep.api.operations.file.download.DownloadFileInput;
import com.personal.mavrep.api.operations.file.download.DownloadFileOperation;
import com.personal.mavrep.api.operations.file.download.DownloadFileResult;
import com.personal.mavrep.api.operations.file.upload.UploadFileInput;
import com.personal.mavrep.api.operations.file.upload.UploadFileOperation;
import com.personal.mavrep.api.operations.file.upload.UploadFileResult;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FileController extends BaseController {
    private final UploadFileOperation uploadFile;
    private final DownloadFileOperation downloadFile;

    //TODO: replace "/mvn" with real repo name
    @GetMapping(path = "/mvn/**", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> get(HttpServletRequest request, HttpServletResponse response) {
//
//        DownloadFileInput input = DownloadFileInput
//                .builder()
//                .uri(request.getRequestURI())
//                .build();
//
//        Either<? extends BaseApiError, DownloadFileResult> process = this.downloadFile.process(input);
//
//        if (process.isLeft()) {
//            ((HttpServletResponseImpl) response).getExchange().setReasonPhrase(process.getLeft().getMessage());
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//


//        return ResponseEntity.ok(process.get().getContent());

        return ResponseEntity.notFound().build();
    }

    //TODO: replace "/mvn" with real repo name
    @PutMapping(path = "/mvn/**")
    public ResponseEntity<?> put(@RequestBody byte[] content, HttpServletRequest request, HttpServletResponse response) {

        UploadFileInput input = UploadFileInput
                .builder()
                .uri(request.getRequestURI())
                .content(content)
                .build();

        return this.handle(this.uploadFile.process(input), response);
    }

}
