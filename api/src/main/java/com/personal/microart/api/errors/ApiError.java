package com.personal.microart.api.errors;

public interface ApiError {

    Integer getStatusCode();

    String getStatusMessage();
}
