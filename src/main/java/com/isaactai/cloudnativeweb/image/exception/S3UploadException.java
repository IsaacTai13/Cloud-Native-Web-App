package com.isaactai.cloudnativeweb.image.exception;

import com.isaactai.cloudnativeweb.common.error.BaseApiException;
import com.isaactai.cloudnativeweb.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * @author tisaac
 */
public class S3UploadException extends BaseApiException{
    public S3UploadException() { super(HttpStatus.BAD_GATEWAY, ErrorCode.BAD_GATEWAY, "S3 upload failed"); }
}