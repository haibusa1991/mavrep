package com.personal.mavrep.controllerAdvice;

import com.personal.mavrep.controllers.TestController;
import com.personal.mavrep.exceptions.VersionAlreadyDeployedException;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.FileNotFoundException;


@RestControllerAdvice(basePackageClasses = TestController.class)
public class TestControllerAdvise extends ResponseEntityExceptionHandler {

    @ResponseStatus(code = HttpStatus.CONFLICT)
    @ExceptionHandler(VersionAlreadyDeployedException.class)
    public void handleVersionAlreadyDeployedException(VersionAlreadyDeployedException e, HttpServletResponse response) {
        ((HttpServletResponseImpl) response).getExchange().setReasonPhrase(e.getMessage());
    }

    @ResponseStatus(code = HttpStatus.INSUFFICIENT_STORAGE)
    @ExceptionHandler(FileNotFoundException.class)
    public void handleFileNotFoundException(VersionAlreadyDeployedException e, HttpServletResponse response) {
        ((HttpServletResponseImpl) response).getExchange().setReasonPhrase(e.getMessage());
    }
}
