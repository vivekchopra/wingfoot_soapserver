/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */


package com.wingfoot.soap;

/**
 * Encapsulates the exceptions specific to Wingfoot soap.
 * @since 0.90
 * @author Kal Iyer
 */

public class SOAPException extends Exception
{
  /**
   * Creates instance of SOAPException.
   * @since 0.90
   * @param message the message to encapsulate
   * as an Exception.
   */
  public SOAPException (String message)
  {
    super(message);
  }

}
