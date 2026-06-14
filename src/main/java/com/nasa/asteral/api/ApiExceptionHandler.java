package com.nasa.asteral.api;

import java.time.Instant;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nasa.asteral.configuration.CorrelationIdFilter;
import com.nasa.asteral.exception.NasaApiUnavailableException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice(basePackageClasses = AsteroidApiController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(NasaApiUnavailableException.class)
    ResponseEntity<ApiError> handleNasaUnavailable(
            NasaApiUnavailableException exception,
            HttpServletRequest request) {
        log.warn("NASA API request failed", exception);
        return error(HttpStatus.SERVICE_UNAVAILABLE, "NASA_API_UNAVAILABLE", exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled API request failure", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "The request could not be completed.", request);
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                MDC.get(CorrelationIdFilter.MDC_KEY));
        return ResponseEntity.status(status).body(body);
    }
}
