package com.runinto.exception.event;

public class PermissionDeniedException  extends RuntimeException{
    public PermissionDeniedException(String message) {
        super(message);
    }
}
