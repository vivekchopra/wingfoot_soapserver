/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */


package com.wingfoot.soap.server;

/**
 * Encapsulates the exceptions in the SOAP Router
 * @author Baldwin Louie
 */
import com.wingfoot.soap.*;
public class RouterException extends Exception
{ 
  /**
   * Variable to capture the place where the
   * exception was created.  This is used
   * while generation the faultcode element.
   * Possible values are taken from the public
   * variables in Fault object.
   */

  private String exceptionType = Fault.SERVER;
  /**
   * Creates instance of RouterException.
   * @param message the text to encapsulate
   * as an Exception.
   */
  public RouterException (String message)
  {
    super(message);
  }

  public void setExceptionType(String type) {
    this.exceptionType=type;
  }

  public String getExceptionType() {
    return exceptionType;
  }

}
