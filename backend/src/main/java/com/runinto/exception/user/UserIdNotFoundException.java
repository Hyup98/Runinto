package com.runinto.exception.user;

public class UserIdNotFoundException extends RuntimeException {
    public UserIdNotFoundException(String message) {
        super(message);
    }
}