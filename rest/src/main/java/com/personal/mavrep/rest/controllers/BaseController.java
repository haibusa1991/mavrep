package com.personal.mavrep.rest.controllers;

import com.personal.mavrep.api.errors.ApiError;
import com.personal.mavrep.api.operations.file.download.DownloadFileResult;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {

    public ResponseEntity<?> handle(Either<ApiError, ?> processorResult, HttpServletResponse response) {
        if (processorResult.isLeft()) {
            ApiError error = processorResult.getLeft();
            ((HttpServletResponseImpl) response)
                    .getExchange()
                    .setReasonPhrase(error.getStatusMessage());

            return ResponseEntity
                    .status(error.getStatusCode())
                    .build();
        }

        return new ResponseEntity<>(processorResult.get(), HttpStatus.OK);
    }

    public ResponseEntity<byte[]> handleFile(Either<ApiError, DownloadFileResult> processorResult, HttpServletResponse response) {
        if (processorResult.isLeft()) {
            ApiError error = processorResult.getLeft();
            ((HttpServletResponseImpl) response)
                    .getExchange()
                    .setReasonPhrase(error.getStatusMessage());

            return ResponseEntity
                    .status(error.getStatusCode())
                    .build();
        }

        return new ResponseEntity<>(processorResult.get().getContent(), HttpStatus.OK);
    }


}
