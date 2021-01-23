package com.github.votingsessionmanager.exception;

public class ClosedAgendaException extends RuntimeException {
    public ClosedAgendaException() {
        super("Agenda is closed.");
    }
}
