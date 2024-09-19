package com.orbix.api.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class ErrorResponse {
	
	private int status;
	private String statusText;
    private String message;
    private String details;
    private long timestamp;

    public ErrorResponse(int status, String statusText, String message, String details) {
        this.status = status;
        this.statusText = statusText;
        this.message = message;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
    }
}
