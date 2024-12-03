package com.example.jobapi.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends RuntimeException {
    private final HttpStatus status = HttpStatus.BAD_REQUEST;

    public InvalidRequestException(String message) {
        super(message);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
