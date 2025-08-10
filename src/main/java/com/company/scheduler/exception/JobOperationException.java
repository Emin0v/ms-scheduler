package com.company.scheduler.exception;

import static com.company.scheduler.exception.model.constant.ErrorCode.INTERNAL_SERVER;

import java.text.MessageFormat;

public class JobOperationException extends CommonException {

    public JobOperationException(String errorMessage) {
        super(INTERNAL_SERVER, errorMessage);
    }

    public static JobOperationException of(String message, Object... args) {
        return new JobOperationException(MessageFormat.format(message, args));
    }
}
