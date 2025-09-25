package com.isaactai.cloudnativeweb.user.exception;

import com.isaactai.cloudnativeweb.common.error.BaseApiException;
import com.isaactai.cloudnativeweb.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * @author tisaac
 */
public class DuplicateEmailException extends BaseApiException {
    public DuplicateEmailException() {
        super(HttpStatus.BAD_REQUEST, ErrorCode.DUPLICATE_EMAIL, "Email already exists");
    }
}
