/**
 * 
 */
package com.orbix.api.exceptions;

/**
 * @author GODFREY
 *
 */
public class MissingInformationException extends RuntimeException{
	private static final long serialVersionUID = 6L;
	public String message;
	public MissingInformationException(String message){
		this.message = message;
	}
}
