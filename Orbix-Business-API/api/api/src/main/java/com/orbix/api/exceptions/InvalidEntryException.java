/**
 * 
 */
package com.orbix.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author GODFREY
 *
 */
public class InvalidEntryException extends RuntimeException{
	private static final long serialVersionUID = 3L;
	public String message;
	
	public InvalidEntryException(String message){
		super(message);
	}
}
