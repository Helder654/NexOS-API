package com.example.nexos.exceptions;

public class InvalidServiceOrderStatusException extends RuntimeException {

    public InvalidServiceOrderStatusException(String message) {
        super(message);
    }

}
