package com.personal.microart.rest.controllers;

import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.operations.file.download.DownloadFileResult;
import io.undertow.server.HttpServerExchange;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class BaseController {

    @Setter
    private ExchangeAccessor exchangeAccessor;

    public ResponseEntity<?> handle(Either<ApiError, ?> processorResult, HttpServletResponse response) {
        return processorResult.isLeft()
                ? this.handleError(processorResult, response)
                : new ResponseEntity<>(processorResult.get(), HttpStatus.OK);
    }

    public ResponseEntity<?> handle(Either<ApiError, ?> processorResult, HttpServletResponse response, HttpStatus status) {
        return processorResult.isLeft()
                ? this.handleError(processorResult, response)
                : new ResponseEntity<>(processorResult.get(), status);
    }

    public ResponseEntity<byte[]> handleMvn(Either<ApiError, DownloadFileResult> processorResult, HttpServletResponse response) {
        if (processorResult.isLeft()) {
            return this.handleMvnError(processorResult, response);
        }

        response.setHeader("Content-Disposition", "attachment; filename=" + processorResult.get().getFilename());

        return new ResponseEntity<>(processorResult.get().getContent(), HttpStatus.OK);
    }


    private ResponseEntity<?> handleError(Either<ApiError, ?> processorResult, HttpServletResponse response) {
        ApiError error = processorResult.getLeft();
        HttpServerExchange exchange = this.exchangeAccessor.getExchange(response);

        exchange.setReasonPhrase(error.getStatusMessage());

        ErrorWrapper wrapper = ErrorWrapper.builder()
                .errorCode(error.getStatusCode())
                .uri(exchange.getRequestURI())
                .dateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .errors(List.of(error.getStatusMessage().split(",\\s*")))
                .build();

        return ResponseEntity
                .status(error.getStatusCode())
                .body(wrapper);
    }


    private ResponseEntity<byte[]> handleMvnError(Either<ApiError, ?> processorResult, HttpServletResponse response) {
        ApiError error = processorResult.getLeft();
        this.exchangeAccessor.getExchange(response)
                .setReasonPhrase(error.getStatusMessage());

        return ResponseEntity
                .status(error.getStatusCode())
                .build();
    }
}
