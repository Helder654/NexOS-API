package com.example.nexos.exceptions;

public class InvalidUserOperationException extends RuntimeException {

    public InvalidUserOperationException(String message) {
        super(message);
    }
}
