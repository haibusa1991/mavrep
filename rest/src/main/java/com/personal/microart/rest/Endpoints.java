package com.personal.microart.rest;

public class Endpoints {
    public final static String BROWSE = "/browse";
    public final static String BROWSE_FILES = BROWSE + "/**";

    public final static String FILE = "/mvn";
    public final static String FILE_DOWNLOAD = FILE + "/**";
    public final static String FILE_UPLOAD = FILE + "/**";

    public final static String TEST = "/test";
    public final static String TEST_JWT = TEST + "/jwt-auth";

    public final static String USER = "/user";
    public final static String USER_REGISTER = USER + "/register";
    public final static String USER_LOGIN = USER + "/login";
    public final static String USER_REQUEST_PASSWORD = USER + "/request-password";
    public final static String USER_RESET_PASSWORD = USER + "/password-reset";
    public final static String USER_LOGOUT = USER + "/logout";

    public final static String VAULT = "/vault";
    public final static String MODIFY_VAULT_AUTH_USER = VAULT + "/{vaultName}/user";

}