package com.personal.microart.core.email.sender;

import lombok.Getter;

/**
 * The EmailParameter enum represents the parameters that can be used in an email. List can be expanded as needed.
 * Required by the {@link com.personal.microart.core.email.factory.EmailFactory EmailFactory}
 */
public enum EmailParameter {

    /**
     * The recipient's email address, e.g. "my.user@abv.bg"
     */
    TO("to"),

    /**
     * The subject of the email, e.g. "Account activation email"
     */
    SUBJECT("subject"),

    /**
     * The under which the user has registered, e.g. "MyUser"
     */
    USERNAME("username"),


    /**
     * A password recovery token in case user has forgotten their password, e.g. "fdiuh_gGDSdsf2"
     */
    PASSWORD_RECOVERY_TOKEN("recoveryToken"),

    /**
     * The validity of the password recovery token in minutes, e.g. "30"
     */
    TOKEN_VALIDITY("tokenValidity");

    @Getter
    private final String value;

    EmailParameter(String value) {
        this.value = value;
    }

}
