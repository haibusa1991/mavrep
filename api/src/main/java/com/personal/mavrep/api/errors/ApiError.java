package com.personal.mavrep.api.errors;

public interface ApiError {

    Integer getStatusCode();

    String getStatusMessage();
}
