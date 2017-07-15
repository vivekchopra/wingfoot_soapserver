/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */


package com.wingfoot.soap.server;

/**
 * Encapsulates the exceptions that occurs
 * while deploying a service
 * @author Kal Iyer
 */

public class DeploymentException extends Exception
{
  /**
   * Creates instance of DeploymentException.
   * @param message the message to encapsulate
   * as an Exception.
   */
  public DeploymentException (String message)
  {
    super(message);
  }

}
