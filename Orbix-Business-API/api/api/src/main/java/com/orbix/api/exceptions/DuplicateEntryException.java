/**
 * 
 */
package com.orbix.api.exceptions;

/**
 * @author GODFREY
 *
 */
public class DuplicateEntryException extends RuntimeException{
	private static final long serialVersionUID = 2L;
	public String message;
	public DuplicateEntryException(String message){
		this.message = message;
	}

}
