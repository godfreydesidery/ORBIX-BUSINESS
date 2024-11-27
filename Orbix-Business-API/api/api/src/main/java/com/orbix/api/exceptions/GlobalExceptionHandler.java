package com.orbix.api.exceptions;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	// Handle specific exception (e.g., Resource Not Found)
    @ExceptionHandler(InvalidEntryException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEntryException(InvalidEntryException ex, WebRequest request) {
        
    	ErrorResponse errorResponse = new ErrorResponse(
    			HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.toString(),
                ex.getMessage(),
                request.getDescription(false));
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex, WebRequest request) {
        
    	ErrorResponse errorResponse = new ErrorResponse(
    			HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.toString(),
                ex.getMessage(),
                request.getDescription(false));
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Handle global exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        
    	ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                //"Internal Server Error",
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
