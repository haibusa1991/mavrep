package com.personal.microart.rest.controllers;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
/**
 * POJO wrapper for error messages.
 */
@AllArgsConstructor
@Builder
@Getter
public class ErrorWrapper {

    private Integer errorCode;
    private String uri;
    private String dateTime;
    private List<String> errors;
}
