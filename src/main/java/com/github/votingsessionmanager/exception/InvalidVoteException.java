package com.github.votingsessionmanager.exception;

public class InvalidVoteException extends RuntimeException {
    public InvalidVoteException(String message) {
        super(message);
    }
}
