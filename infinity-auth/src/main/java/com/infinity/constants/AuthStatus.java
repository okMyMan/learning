package com.infinity.constants;

/**
 * AuthStatus
 *
 * @author Alvin Xu
 * @date 2016/9/12
 */
public enum AuthStatus {

    CHECK_TOKEN_EXPIRED(-1, "The token is expired!"),
    CHECK_TOKEN_FAILURE(0, "Token check failed!"),
    SUCCESS(1, ""),
    PERMISSION_DENIED(2, "permission denied!"),
    RUNTIME_EXCEPTION(3, "runtime exception!"),
    GENERATE_TOKEN_FAILURE(5,"Token generate failed."),
    CHECK_TOKEN_LOGDOUT(6, "User has logged out!"),

    ;


    AuthStatus(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int status;
    public String message;

}
