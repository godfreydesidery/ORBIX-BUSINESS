/**
 * 
 */
package com.orbix.api.exceptions;

/**
 * @author GODFREY
 *
 */
public class InvalidOperationException extends RuntimeException{
	private static final long serialVersionUID = 4L;
	public String message;
	public InvalidOperationException(String message){
		// Passed on too, so getMessage() carries it whichever handler answers (it was null before)
		super(message);
		this.message = message;
	}
}
