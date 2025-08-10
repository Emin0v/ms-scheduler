package com.company.scheduler.exception;

import static com.company.scheduler.exception.model.constant.ErrorCode.DATA_NOT_FOUND;
import static com.company.scheduler.exception.model.constant.ErrorCode.INTERNAL_SERVER;
import static com.company.scheduler.exception.model.constant.ErrorCode.REQUEST_INVALID;
import static com.company.scheduler.exception.model.constant.ErrorMessage.INTERNAL_SERVER_ERROR_MESSAGE;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;
import static net.logstash.logback.argument.StructuredArguments.v;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.company.scheduler.exception.model.ErrorResponse;
import com.company.scheduler.exception.model.constant.ErrorCode;
import com.company.scheduler.util.MessageSourceUtil;
import com.company.scheduler.util.WebUtil;
import jakarta.validation.ConstraintViolationException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Component
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private WebUtil webUtil;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDataNotFoundException(DataNotFoundException ex) {
        addErrorLog(NOT_FOUND, ex.getMessage(), "DataNotFoundException");

        return ResponseEntity.status(NOT_FOUND)
                .body(ErrorResponse.of(DATA_NOT_FOUND, ex.getErrorMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        addErrorLog(BAD_REQUEST, ex.getMessage(), "MethodArgumentTypeMismatchException");

        return ResponseEntity.status(BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, ex.getMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String errMsg = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + messageSourceUtil.getMessage(v.getMessage()))
                .collect(collectingAndThen(joining("; "), Optional::ofNullable))
                .orElse(ex.getMessage());

        addErrorLog(BAD_REQUEST, errMsg, "ConstraintViolationException");
        return ResponseEntity.status(BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, errMsg, webUtil.getRequestId()));
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException ex) {
        addErrorLog(BAD_REQUEST, ex.getErrorCode(), ex.getErrorMessage(), "InvalidInputException");
        return ResponseEntity.status(BAD_REQUEST)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getErrorMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExistException(AlreadyExistException ex) {
        addErrorLog(BAD_REQUEST, ex.getErrorCode(), ex.getErrorMessage(), "AlreadyExistException");
        return ResponseEntity.status(BAD_REQUEST)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getErrorMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ErrorResponse> handleCommonException(CommonException ex) {
        addErrorLog(INTERNAL_SERVER_ERROR, ex.getErrorCode(), ex.getMessage(), ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        addErrorLog(INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getMessage(), ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(INTERNAL_SERVER, INTERNAL_SERVER_ERROR_MESSAGE, webUtil.getRequestId()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode statusCode,
                                                                  WebRequest request) {
        Optional<FieldError> fieldError = ex.getBindingResult().getFieldErrors().stream().findAny();
        String errorMessage;
        String errorCode;
        if (fieldError.isPresent()) {
            String fieldName = fieldError.get().getField();
            String fieldMessage = fieldError.get().getDefaultMessage();
            errorMessage = fieldName + " " + messageSourceUtil.getMessage(fieldMessage);
            errorCode = messageSourceUtil.getErrorCode(fieldMessage, REQUEST_INVALID);
        } else {
            errorMessage = ex.getMessage();
            errorCode = REQUEST_INVALID;
        }
        addErrorLog(httpStatus(statusCode), errorCode, errorMessage, "MethodArgumentNotValidException");
        ErrorResponse errorResponse = new ErrorResponse(errorCode, errorMessage, webUtil.getRequestId());
        return new ResponseEntity<>(errorResponse, headers, statusCode);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex,
                                                                     HttpHeaders headers,
                                                                     HttpStatusCode status, WebRequest request) {
        String error = ex.getRequestPartName() + " parameter is missing";
        addErrorLog((HttpStatus) status, error, "MissingServletRequestParameterException");
        ErrorResponse errorResponse = ErrorResponse.of(status.toString(), error, webUtil.getRequestId());
        return ResponseEntity.status(status).headers(headers).body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        String errLogMessage = String.join(System.lineSeparator(),
                ex.getMessage(), "Request body: " + webUtil.getRequestBody());
        String error = "Request not readable";
        addErrorLog((HttpStatus) status, errLogMessage, "HttpMessageNotReadableException");
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.BAD_REQUEST, error, webUtil.getRequestId());
        return ResponseEntity.status(status).headers(headers).body(errorResponse);
    }

    private String extractFieldNameFromMessage(String message) {
        Pattern pattern = Pattern.compile("Key \\((.*?)\\)");
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1) : null;
    }

    protected HttpStatus httpStatus(HttpStatusCode httpStatusCode) {
        return HttpStatus.resolve(httpStatusCode.value());
    }

    /*** Logging ***/

    protected void addErrorLog(HttpStatus httpStatus, String errorMessage, String message, Throwable ex) {
        addErrorLog(httpStatus.value(), errorMessage, ex);
    }

    protected void addErrorLog(HttpStatus httpStatus, String errorMessage, String exceptionType) {
        addErrorLog(httpStatus.value(), errorMessage, exceptionType);
    }

    protected void addErrorLog(Integer errorCode, String errorMessage, Throwable ex) {
        log.error("[Error] | Code: {} | Type: {} | Path: {} | Elapsed time: {} ms | Message: {}",
                errorCode, ex.getClass().getTypeName(), webUtil.getRequestUri(),
                v("elapsed_time", webUtil.getElapsedTime()), errorMessage, ex);
    }

    protected void addErrorLog(Integer errorCode, String errorMessage, String exceptionType) {
        log.error("[Error] | Code: {} | Type: {} | Path: {} | Elapsed time: {} ms | Message: {}",
                errorCode, exceptionType, webUtil.getRequestUri(),
                v("elapsed_time", webUtil.getElapsedTime()), errorMessage);
    }

    protected void addErrorLog(HttpStatus httpStatus, String errorCode, String errorMessage, String exceptionType) {
        log.error("[Error] | Status: {} | Code: {} | Type: {} | Path: {} | Elapsed time: {} ms | Message: {}",
                httpStatus, errorCode, exceptionType, webUtil.getRequestUri(),
                v("elapsed_time", webUtil.getElapsedTime()), errorMessage);
    }

}
