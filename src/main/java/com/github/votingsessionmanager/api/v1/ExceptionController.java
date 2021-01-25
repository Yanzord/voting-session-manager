package com.github.votingsessionmanager.api.v1;

import com.github.votingsessionmanager.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

    @ExceptionHandler(IdNotFoundException.class)
    public ResponseEntity<String> handleNotFound(RuntimeException ex) {
        logger.info(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            AgendaStatusException.class,
            SessionStatusException.class,
            RequiredFieldException.class,
            InvalidVoteException.class
    })
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        logger.info(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
