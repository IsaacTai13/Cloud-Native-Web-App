package com.isaactai.cloudnativeweb.common.exception;

import com.isaactai.cloudnativeweb.common.error.BaseApiException;
import org.springframework.http.HttpStatus;
import com.isaactai.cloudnativeweb.common.error.ErrorCode;

/**
 * @author tisaac
 */
public class ForbiddenException extends BaseApiException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, message);
    }
}
