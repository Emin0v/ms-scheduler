package com.company.scheduler.exception.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

@Value
@Getter
@AllArgsConstructor
public class ErrorResponse {

    String errorCode;
    String errorMessage;
    String requestId;
    LocalDateTime timestamp;

    public ErrorResponse(String errorCode, String errorMessage, String requestId) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.requestId = requestId;
        this.timestamp = LocalDateTime.now();
    }

    public static ErrorResponse of(String errorCode, String errorMessage, String requestId) {
        return new ErrorResponse(errorCode, errorMessage, requestId);
    }

}
