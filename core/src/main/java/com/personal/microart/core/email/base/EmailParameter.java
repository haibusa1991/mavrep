package com.personal.microart.core.email.base;

import lombok.Getter;

/**
 * The EmailParameter enum represents the parameters that can be used in an email. List can be expanded as needed.
 * Required by the EmailFactory.
 */
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
