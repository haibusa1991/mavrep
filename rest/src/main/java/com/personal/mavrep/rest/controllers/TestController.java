package com.personal.mavrep.rest.controllers;

import com.personal.mavrep.api.exceptions.MavrepException;
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
public class TestController {
    private final UploadFileOperation uploadFile;
    private final DownloadFileOperation downloadFile;

    @GetMapping(path = "/mvn/**", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> get(HttpServletRequest request, HttpServletResponse response) {

        DownloadFileInput input = DownloadFileInput
                .builder()
                .uri(request.getRequestURI())
                .build();

        Either<? extends MavrepException, DownloadFileResult> process = this.downloadFile.process(input);

        if (process.isLeft()) {
            ((HttpServletResponseImpl) response).getExchange().setReasonPhrase(process.getLeft().getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(process.get().getContent());
    }

    @PutMapping(path = "/mvn/**")
    public ResponseEntity<String> put(@RequestBody byte[] content, HttpServletRequest request, HttpServletResponse response) {

        UploadFileInput input = UploadFileInput
                .builder()
                .uri(request.getRequestURI())
                .content(content)
                .build();

        Either<? extends MavrepException, UploadFileResult> process = this.uploadFile.process(input);

        if (process.isLeft()) {
            ((HttpServletResponseImpl) response).getExchange().setReasonPhrase(process.getLeft().getMessage());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
