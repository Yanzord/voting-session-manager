package com.github.votingsessionmanager.exception;

public class RequiredAgendaIdException extends RuntimeException {
    public RequiredAgendaIdException() {
        super("Agenda Id is required to open session.");
    }
}
