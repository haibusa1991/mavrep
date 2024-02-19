package com.personal.microart.core.processor;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class UriProcessor {

    public String getUsername(String uri) {
        return this.getUriElements(uri)[1];
    }

    public String getVaultName(String uri) {
        String[] uriElements = this.getUriElements(uri);

        if (uriElements.length < 3) {
            return "";
        }

        return uriElements[2];
    }

    //suggest unit test
    private String[] getUriElements(String uri) {
        return Arrays.stream(uri.split("/"))
                .filter(element -> !element.isBlank())
                .toArray(String[]::new);
    }
}