package com.orbix.api.api.commons;

import lombok.Data;

@Data
public class ApiCustomResponse {
	private int status;
	private String statusText;
    private String message;
    private Object details;
    private long timestamp;
    
    public ApiCustomResponse(int status, String statusText, String message, Object details) {
    	this.status = status;
        this.statusText = statusText;
        this.message = message;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
    }   
}
