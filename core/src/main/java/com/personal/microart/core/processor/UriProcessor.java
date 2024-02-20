package com.personal.microart.core.processor;

import org.springframework.stereotype.Component;

import java.util.Arrays;
/**
 * Helper class responsible for extracting username and vault name from a given URI. Requires /mvn/username/vaultName/...
 * format. No validation is performed, so the input should be validated before calling any of the methods.
 */
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

    private String[] getUriElements(String uri) {
        return Arrays.stream(uri.split("/"))
                .filter(element -> !element.isBlank())
                .toArray(String[]::new);
    }
}