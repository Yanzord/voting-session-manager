package com.github.votingsessionmanager.exception;

public class SessionCreatedException extends RuntimeException {
    public SessionCreatedException() {
        super("Session already created.");
    }
}
