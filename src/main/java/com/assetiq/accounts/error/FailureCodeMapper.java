package com.assetiq.accounts.error;


import org.springframework.http.HttpStatus;

public enum FailureCodeMapper {
    identity_not_found("identity_not_found", HttpStatus.NOT_FOUND),
    ;
    private final String code;
    private final HttpStatus status;
    FailureCodeMapper(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public static HttpStatus fromCode(String code) {
        for (FailureCodeMapper failureCodeMapper : values()) {
            if (failureCodeMapper.code.equals(code)) {
                return failureCodeMapper.status;
            }
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
