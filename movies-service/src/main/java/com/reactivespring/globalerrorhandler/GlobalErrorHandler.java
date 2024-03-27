package com.reactivespring.globalerrorhandler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.reactivespring.exception.MovieInfoClientException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    @ExceptionHandler
    public ResponseEntity<String> handleClientException(MovieInfoClientException exception) {

        log.error("Exception Caught in handleClientException: {}", exception.getMessage());
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
    }
}
