package com.personal.microart.rest.controllers;

import com.personal.microart.api.operations.file.download.DownloadFileInput;
import com.personal.microart.api.operations.file.download.DownloadFileOperation;
import com.personal.microart.api.operations.file.upload.UploadFileInput;
import com.personal.microart.api.operations.file.upload.UploadFileOperation;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * A controller that is responsible for handling file upload and download requests.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/mvn")
public class FileController extends BaseController {
    private final UploadFileOperation uploadFile;
    private final DownloadFileOperation downloadFile;
    private final ExchangeAccessor exchangeAccessor;

    @PostConstruct
    private void setExchangeAccessor() {
        super.setExchangeAccessor(exchangeAccessor);
    }

    @GetMapping(path = "/**", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request, HttpServletResponse response) {

        DownloadFileInput input = DownloadFileInput
                .builder()
                .authentication(request.getHeader(HttpHeaders.AUTHORIZATION))
                .uri(request.getRequestURI())
                .build();

        return this.handleMvn(this.downloadFile.process(input), response);
    }

    @PutMapping(path = "/**")
    public ResponseEntity<?> put(@RequestBody byte[] content, HttpServletRequest request, HttpServletResponse response) {

        UploadFileInput input = UploadFileInput
                .builder()
                .uri(request.getRequestURI())
                .content(content)
                .authentication(request.getHeader(HttpHeaders.AUTHORIZATION))
                .build();

        return this.handle(this.uploadFile.process(input), response);
    }
}
