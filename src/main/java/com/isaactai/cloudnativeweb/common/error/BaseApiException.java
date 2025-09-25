package com.isaactai.cloudnativeweb.common.error;

import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base class for all custom API exceptions.
 *
 *  <p>Extends {@link RuntimeException} so it is unchecked and can
 *  bubble up to the global {@code ApiExceptionHandler} without
 *  requiring explicit try/catch.
 * @author tisaac
 */
@Getter
public abstract class BaseApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode code;

    public BaseApiException(HttpStatus status, ErrorCode code, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
