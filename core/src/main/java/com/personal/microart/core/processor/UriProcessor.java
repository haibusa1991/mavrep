package com.personal.microart.core.processor;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class UriProcessor {

    public String getUsername(String uri) {
        return this.getUriElements(uri)[1];
    }

    public String getVaultName(String uri) {
        return this.getUriElements(uri)[2];
    }

    //suggest unit test
    private String[] getUriElements(String uri) {
        return Arrays.stream(uri.split("/"))
                .filter(element -> !element.isBlank())
                .toArray(String[]::new);
    }
}