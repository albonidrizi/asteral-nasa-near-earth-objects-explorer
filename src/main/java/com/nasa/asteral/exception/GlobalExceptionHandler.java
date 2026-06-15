package com.nasa.asteral.exception;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.nasa.asteral.configuration.CorrelationIdFilter;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NasaApiUnavailableException.class)
    public String handleNasaUnavailable(NasaApiUnavailableException exception, Model model) {
        log.warn("NASA API unavailable", exception);
        addError(model, "NASA data is temporarily unavailable. Please try again later.");
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoResourceFoundException exception, Model model) {
        log.debug("Resource not found: {}", exception.getResourcePath());
        addError(model, "The requested page or resource was not found.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception exception, Model model) {
        log.error("Unhandled request failure", exception);
        addError(model, "An unexpected error occurred. Please try again later.");
        return "error";
    }

    private void addError(Model model, String message) {
        model.addAttribute("errorMessage", message);
        model.addAttribute("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
    }
}
