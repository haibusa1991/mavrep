package com.personal.mavrep.rest.controllers;

import com.personal.mavrep.api.operations.file.download.DownloadFileInput;
import com.personal.mavrep.api.operations.file.download.DownloadFileOperation;
import com.personal.mavrep.api.operations.file.upload.UploadFileInput;
import com.personal.mavrep.api.operations.file.upload.UploadFileOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class FileController extends BaseController {
    private final UploadFileOperation uploadFile;
    private final DownloadFileOperation downloadFile;

    //TODO: replace "/mvn" with real repo name
    @GetMapping(path = "/mvn/**", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request, HttpServletResponse response) {

        DownloadFileInput input = DownloadFileInput
                .builder()
                .uri(request.getRequestURI())
                .build();

        return this.handleFile(this.downloadFile.process(input), response);
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
