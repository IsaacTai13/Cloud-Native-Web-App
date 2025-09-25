package com.isaactai.cloudnativeweb.common.exception;

import com.isaactai.cloudnativeweb.common.error.BaseApiException;
import com.isaactai.cloudnativeweb.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * @author tisaac
 */
public class NotFoundException extends BaseApiException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, message);
    }
}
