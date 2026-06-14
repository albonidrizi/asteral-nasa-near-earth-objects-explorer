package com.nasa.asteral.exception;

public class NasaApiUnavailableException extends RuntimeException {

    public NasaApiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
