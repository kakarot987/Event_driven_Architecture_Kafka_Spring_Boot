package com.eventDrivenKafka.util.exceptions;

public class ResourceNotFoundException extends RuntimeException{
	
  public ResourceNotFoundException() {
	  
  }
  public ResourceNotFoundException(String msg) {
	    super(msg);
	  }
}
