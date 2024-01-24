package com.personal.mavrep.rest.controllers;

import com.personal.mavrep.api.errors.ApiError;
import com.personal.mavrep.api.operations.file.download.DownloadFileResult;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import io.vavr.control.Either;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class BaseController {

    public ResponseEntity<?> handle(Either<ApiError, ?> processorResult, HttpServletResponseImpl response) {
        return processorResult.isLeft()
                ? this.handleError(processorResult, response)
                : new ResponseEntity<>(processorResult.get(), HttpStatus.OK);
    }

    public ResponseEntity<?> handle(Either<ApiError, ?> processorResult, HttpServletResponseImpl response, HttpStatus status) {
        return processorResult.isLeft()
                ? this.handleError(processorResult, response)
                : new ResponseEntity<>(processorResult.get(), status);
    }

    @SneakyThrows
    public ResponseEntity<byte[]> handleFile(Either<ApiError, DownloadFileResult> processorResult, HttpServletResponseImpl response) {
        if (processorResult.isLeft()) {
            return (ResponseEntity<byte[]>) this.handleError(processorResult, response);
        }

        response.setHeader("Content-Disposition", "attachment; filename=" + processorResult.get().getFilename());

        return new ResponseEntity<>(processorResult.get().getContent(), HttpStatus.OK);
    }


    private ResponseEntity<?> handleError(Either<ApiError, ?> processorResult, HttpServletResponseImpl response) {
        ApiError error = processorResult.getLeft();
        response
                .getExchange()
                .setReasonPhrase(error.getStatusMessage());

        ErrorWrapper wrapper = ErrorWrapper.builder()
                .errorCode(error.getStatusCode())
                .uri(response.getExchange().getRequestURI())
                .dateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")))
                .errors(List.of(error.getStatusMessage().split(",\\s*")))
                .build();

        return ResponseEntity
                .status(error.getStatusCode())
                .body(wrapper);
    }
}
