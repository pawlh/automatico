package edu.byu.cs.controller.exception;

public class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
