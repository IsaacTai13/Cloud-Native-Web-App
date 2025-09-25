package com.isaactai.cloudnativeweb.product.exception;

import com.isaactai.cloudnativeweb.common.error.BaseApiException;
import com.isaactai.cloudnativeweb.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * @author tisaac
 */
public class DuplicateSkuException extends BaseApiException{
    public DuplicateSkuException() {
        super(HttpStatus.BAD_REQUEST, ErrorCode.DUPLICATE_SKU, "Sku already exists");
    }
}