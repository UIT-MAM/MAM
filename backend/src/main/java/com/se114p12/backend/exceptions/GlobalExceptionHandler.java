package com.se114p12.backend.exceptions;

import com.se114p12.backend.enums.ErrorType;
import com.se114p12.backend.vo.ErrorVO;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataConflictException.class)
    public ResponseEntity<?> handleDataConflictException(DataConflictException e) {
        Map<String, Object> errorDetails = new HashMap<>();
        for (var entry : e.getErrors().entrySet()) {
            errorDetails.put(entry.getKey(), entry.getValue());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        ErrorVO.builder()
                                .type(ErrorType.RESOURCE_CONFLICT_ERROR)
                                .details(errorDetails)
                                .build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorVO.builder()
                                .type(ErrorType.RESOURCE_NOT_FOUND_ERROR)
                                .details(Map.of("message", e.getMessage()))
                                .build());
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorVO.builder()
                                .type(ErrorType.BAD_REQUEST_ERROR)
                                .details(Map.of("message", e.getMessage()))
                                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        var errors = e.getBindingResult().getFieldErrors();
        Map<String, Object> errorMap = new HashMap<>();
        for (var error : errors) {
            errorMap.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorVO.builder().type(ErrorType.VALIDATION_ERROR).details(errorMap).build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handle(Exception e) {
        e.printStackTrace(); // log ra console
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}
