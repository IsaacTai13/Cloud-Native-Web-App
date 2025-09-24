package com.isaactai.cloudnativeweb.config;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.isaactai.cloudnativeweb.common.error.ApiErrorResponse;
import com.isaactai.cloudnativeweb.common.error.BaseApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST APIs.
 *
 * <p>Intercepts any exceptions thrown from controllers or services
 *  * that are subclasses of {@link BaseApiException}, and converts
 *  * the exception data into a standardized HTTP response.
 * @author tisaac
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    // For my custom BaseApiException
    @ExceptionHandler(BaseApiException.class)
    public ResponseEntity<ApiErrorResponse> handleBase(BaseApiException ex, HttpServletRequest req) {
        HttpStatus status = ex.getStatus();
        ApiErrorResponse body = ApiErrorResponse.of(
                status.value(),
                ex.getCode().name(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
