package com.github.votingsessionmanager.exception;

public class SessionNotOpenedException extends RuntimeException {
    public SessionNotOpenedException() {
        super("There's no opened session for the given agenda.");
    }
}
