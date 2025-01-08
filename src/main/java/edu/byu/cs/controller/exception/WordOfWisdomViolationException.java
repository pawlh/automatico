package edu.byu.cs.controller.exception;

public class WordOfWisdomViolationException extends Exception {
    private static final String shortAndStout = "yo im a teapot";

    public WordOfWisdomViolationException(String message) {
        super(message);
    }
}
