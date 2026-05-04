package com.example.ToneFit.common.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Long sessionId;

    public ApiException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage(), null, null);
    }

    public ApiException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, null);
    }

    public ApiException(ErrorCode errorCode, String message, Long sessionId) {
        this(errorCode, message, sessionId, null);
    }

    public ApiException(ErrorCode errorCode, String message, Long sessionId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.sessionId = sessionId;
    }
}
