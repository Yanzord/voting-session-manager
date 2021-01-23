package com.github.votingsessionmanager.api.v1;

import com.github.votingsessionmanager.exception.ClosedAgendaException;
import com.github.votingsessionmanager.exception.IdNotFoundException;
import com.github.votingsessionmanager.exception.OpenedSessionException;
import com.github.votingsessionmanager.exception.RequiredAgendaIdException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(IdNotFoundException.class)
    public ResponseEntity<String> handleNotFound(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ ClosedAgendaException.class, OpenedSessionException.class, RequiredAgendaIdException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
