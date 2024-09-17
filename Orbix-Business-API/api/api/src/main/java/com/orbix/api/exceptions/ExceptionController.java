/**
 * 
 */
package com.orbix.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author GODFREY
 *
 */
@ControllerAdvice
public class ExceptionController {
	@ExceptionHandler(value = NotFoundException.class)
	   public ResponseEntity<Object> exception(NotFoundException exception) {
	      return new ResponseEntity<>("Not found: "+exception.message, HttpStatus.NOT_FOUND);
	   }
	@ExceptionHandler(value = DuplicateEntryException.class)
	   public ResponseEntity<Object> exception(DuplicateEntryException exception) {
	      return new ResponseEntity<>("Duplicate entry: "+exception.message, HttpStatus.CONFLICT);
	   }
	@ExceptionHandler(value = InvalidEntryException.class)
	   public ResponseEntity<Object> exception(InvalidEntryException exception) {
	      return new ResponseEntity<>("Invalid entry: "+exception.message, HttpStatus.CONFLICT);
	   }
	@ExceptionHandler(value = InvalidOperationException.class)
	   public ResponseEntity<Object> exception(InvalidOperationException exception) {
	      return new ResponseEntity<>("Invalid operation: "+exception.message, HttpStatus.CONFLICT);
	   }
	@ExceptionHandler(value = MissingInformationException.class)
	   public ResponseEntity<Object> exception(MissingInformationException exception) {
	      return new ResponseEntity<>("Missing information: "+exception.message, HttpStatus.EXPECTATION_FAILED);
	   }
	@ExceptionHandler(value = OperationFailedException.class)
	   public ResponseEntity<Object> exception(OperationFailedException exception) {
	      return new ResponseEntity<>("Operation failed: "+exception.message, HttpStatus.EXPECTATION_FAILED);
	   }
	
	

}
