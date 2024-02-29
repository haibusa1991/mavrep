package com.personal.microart.core.email.base;

import lombok.Getter;

public enum EmailParameter {
    TO("to"),
    SUBJECT("subject"),
    USERNAME("username"),
    PASSWORD_RECOVERY_TOKEN("recoveryToken"),
    TOKEN_VALIDITY("tokenValidity");

    @Getter
    private final String value;

    EmailParameter(String value) {
        this.value = value;
    }

}
