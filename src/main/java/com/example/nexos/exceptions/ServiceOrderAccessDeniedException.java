package com.example.nexos.exceptions;

public class ServiceOrderAccessDeniedException extends RuntimeException {

    public ServiceOrderAccessDeniedException(String message) {
        super(message);
    }
}
