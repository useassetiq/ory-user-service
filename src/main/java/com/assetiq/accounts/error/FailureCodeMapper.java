package com.assetiq.accounts.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum FailureCodeMapper {
    identity_not_found("identity_not_found", HttpStatus.NOT_FOUND),
    identity_inactive("identity_inactive", HttpStatus.BAD_REQUEST),
    error_getting_identity("error_getting_identity", HttpStatus.INTERNAL_SERVER_ERROR),
    error_updating_identity("error_updating_identity", HttpStatus.INTERNAL_SERVER_ERROR);

    @Getter
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
