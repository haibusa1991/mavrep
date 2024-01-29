package com.personal.microart.rest.controllers;

import com.personal.microart.api.operations.file.download.DownloadFileInput;
import com.personal.microart.api.operations.file.download.DownloadFileOperation;
import com.personal.microart.api.operations.file.upload.UploadFileInput;
import com.personal.microart.api.operations.file.upload.UploadFileOperation;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mvn")
public class FileController extends BaseController {
    private final UploadFileOperation uploadFile;
    private final DownloadFileOperation downloadFile;

    @GetMapping(path = "/**", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request, HttpServletResponseImpl response) {

        DownloadFileInput input = DownloadFileInput
                .builder()
                .uri(request.getRequestURI())
                .build();

        return this.handleMvn(this.downloadFile.process(input), response);


//        return ResponseEntity.status(200).body("Dummy found file");
    }

    @PutMapping(path = "/**")
    public ResponseEntity<?> put(@RequestBody byte[] content, HttpServletRequest request, HttpServletResponseImpl response) {

        UploadFileInput input = UploadFileInput
                .builder()
                .uri(request.getRequestURI())
                .content(content)
                .authentication(request.getHeader("Authorization"))
                .build();

        return this.handle(this.uploadFile.process(input), response);


//        return ResponseEntity.ok().build();
    }

}
