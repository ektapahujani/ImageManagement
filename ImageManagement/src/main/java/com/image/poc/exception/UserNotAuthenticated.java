package com.image.poc.exception;

public class UserNotAuthenticated extends Exception {

    public UserNotAuthenticated(String message) {
        super(message);
    }
}
