package com.personal.mavrep.rest.controllers;

import com.personal.mavrep.api.operations.file.download.DownloadFileInput;
import com.personal.mavrep.api.operations.file.download.DownloadFileOperation;
import com.personal.mavrep.api.operations.file.upload.UploadFileInput;
import com.personal.mavrep.api.operations.file.upload.UploadFileOperation;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
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

        return this.handleFile(this.downloadFile.process(input), response);
    }

    @PutMapping(path = "/**")
    public ResponseEntity<?> put(@RequestBody byte[] content, HttpServletRequest request, HttpServletResponseImpl response) {

        UploadFileInput input = UploadFileInput
                .builder()
                .uri(request.getRequestURI())
                .content(content)
                .build();

        return this.handle(this.uploadFile.process(input), response);
    }

}
