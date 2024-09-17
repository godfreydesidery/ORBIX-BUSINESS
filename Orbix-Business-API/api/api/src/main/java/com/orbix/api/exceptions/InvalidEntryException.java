/**
 * 
 */
package com.orbix.api.exceptions;

/**
 * @author GODFREY
 *
 */
public class InvalidEntryException extends RuntimeException{
	private static final long serialVersionUID = 3L;
	public String message;
	public InvalidEntryException(String message){
		this.message = message;
	}
}
