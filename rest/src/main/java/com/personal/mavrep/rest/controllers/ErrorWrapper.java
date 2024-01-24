package com.personal.mavrep.rest.controllers;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@AllArgsConstructor
@Builder
public class ErrorWrapper {

    private Integer errorCode;
    private String uri;
    private String dateTime;
    private List<String> errors;
}
