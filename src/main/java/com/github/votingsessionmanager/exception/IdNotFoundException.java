package com.github.votingsessionmanager.exception;

public class IdNotFoundException extends RuntimeException {
    public IdNotFoundException() {
        super("Id not found.");
    }
}
