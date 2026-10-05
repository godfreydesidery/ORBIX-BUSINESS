package com.orbix.api.exceptions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.orbix.api.modules.audit.AuditLogService;
import com.orbix.api.modules.audit.AuditRequests;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
	
	// Looked up when first needed
	private final ObjectProvider<AuditLogService> auditLogService;
	
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

        // A refused request (missing privilege) is recorded in the audit log; the response stays the same
        if(ex instanceof AccessDeniedException) {
        	recordAccessDenied(request);
        }
        
    	ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                //"Internal Server Error",
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void recordAccessDenied(WebRequest request) {
    	try {
    		HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
    		String username = servletRequest.getUserPrincipal() == null ? null : servletRequest.getUserPrincipal().getName();
    		auditLogService.getObject().recordAccessDenied(username, servletRequest.getMethod() + " " + servletRequest.getRequestURI(),
    				AuditRequests.ipAddress(servletRequest), AuditRequests.forwardedFor(servletRequest), AuditRequests.userAgent(servletRequest));
    	}catch(Exception e) {
    		// Recording must never change the response
    	}
    }
}
