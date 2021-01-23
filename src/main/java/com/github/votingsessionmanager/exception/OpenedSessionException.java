package com.github.votingsessionmanager.exception;

public class OpenedSessionException extends RuntimeException {
    public OpenedSessionException() {
        super("There's already an opened session for the given agenda.");
    }
}
