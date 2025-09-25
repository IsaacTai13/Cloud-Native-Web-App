package com.isaactai.cloudnativeweb.config;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.isaactai.cloudnativeweb.common.error.ApiErrorResponse;
import com.isaactai.cloudnativeweb.common.error.BaseApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    // @Valid validation failed
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> String.format("%s: %s", err.getField(), err.getDefaultMessage()))
                .distinct()
                .sorted()
                .reduce((a, b) -> "%s; %s".formatted(a, b))
                .orElse("Validation failed");

        return ResponseEntity.badRequest().body(
                ApiErrorResponse.of(400, "VALIDATION_ERROR", msg, req.getRequestURI())
        );
    }

    // Invalid or malformed JSON structure
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleBadJson(
            HttpMessageNotReadableException ex, HttpServletRequest req) {

        Throwable cause = ex.getMostSpecificCause();

        // DTO contains disallowed fields
        if (cause instanceof UnrecognizedPropertyException up) {
            String msg = "Unrecognized field " + up.getPropertyName();
            return ResponseEntity.badRequest().body(
                    ApiErrorResponse.of(400, "BAD_REQUEST", msg, req.getRequestURI()));
        }

        String msg = "Malformed JSON";
        if (cause instanceof com.fasterxml.jackson.core.JsonParseException jpe) {
            msg = "Malformed JSON at line %d, column %d"
                    .formatted(jpe.getLocation().getLineNr(), jpe.getLocation().getColumnNr());
        }
        return ResponseEntity.badRequest().body(
                ApiErrorResponse.of(400, "BAD_REQUEST", msg, req.getRequestURI())
        );
    }

    // TODO: Fallback handler to avoid returning raw 500 errors
}
