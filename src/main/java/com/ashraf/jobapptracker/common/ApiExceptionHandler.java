package com.ashraf.jobapptracker.common;

import com.ashraf.jobapptracker.application.ApplicationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {
    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<Map<String, String>> applicationNotFound(ApplicationNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalidFields(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new TreeMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of(
                "message", "Check the submitted fields.", "errors", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> unreadableBody() {
        return ResponseEntity.badRequest().body(Map.of(
                "message", "Provide valid JSON with correctly typed fields, dates, and status values."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> invalidParameter() {
        return ResponseEntity.badRequest().body(Map.of("message", "Provide a valid numeric application ID."));
    }
}
