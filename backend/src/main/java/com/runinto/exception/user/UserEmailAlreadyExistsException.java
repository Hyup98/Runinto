package com.runinto.exception.user;

public class UserEmailAlreadyExistsException extends RuntimeException {
    public UserEmailAlreadyExistsException(String message) {
        super(message);
    }
}