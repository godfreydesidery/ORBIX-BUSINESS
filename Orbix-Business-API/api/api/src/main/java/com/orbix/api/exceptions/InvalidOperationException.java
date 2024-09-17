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
		this.message = message;
	}
}
