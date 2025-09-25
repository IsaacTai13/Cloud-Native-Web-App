package com.isaactai.cloudnativeweb.common.exception;

import com.isaactai.cloudnativeweb.common.error.BaseApiException;
import com.isaactai.cloudnativeweb.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * @author tisaac
 */
public class BadRequestException extends BaseApiException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, message);
    }
}
