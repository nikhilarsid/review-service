package com.example.demo.exception;

/**
 * Custom exception thrown when a user attempts to perform an action
 * they are not authorized to execute (e.g., deleting another user's review).
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}