package com.personal.microart.rest.controllers;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ErrorWrapper {

    private Integer errorCode;
    private String uri;
    private String dateTime;
    private List<String> errors = new ArrayList<>();
}
