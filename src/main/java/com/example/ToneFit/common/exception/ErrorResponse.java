package com.example.ToneFit.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(Error error) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(new Error(code, message, null));
    }

    public static ErrorResponse of(String code, String message, Long sessionId) {
        return new ErrorResponse(new Error(code, message, sessionId));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Error(String code, String message, Long sessionId) {
    }
}
