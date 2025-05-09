package com.runinto.exception;

public class UserNameAlreadyExistsException extends RuntimeException {
    public UserNameAlreadyExistsException(String message) {
        super(message);
    }
}