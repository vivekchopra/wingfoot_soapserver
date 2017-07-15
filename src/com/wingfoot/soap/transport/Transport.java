/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */


package com.wingfoot.soap.transport;

import com.wingfoot.soap.*;
import java.io.*;
/**
 * Interface for all the transport implementations. Users
 * desiring to implement their own transport have to 
 * implement this interface.
 * @since 0.90
 * @author Kal Iyer
 * @author Vivek Chopra
 */

public interface Transport {

  /**
   * Method to open a connection to a server and send the
   * payload.  Returns a byte stream representing the
   * response from the server. 
   * @param payload the SOAP message as a byte array.
   * @return an array of bytes representing the response;
   * null if no response is returned.
   * throws IOException if any error occurs while connecting
   * to the server.
   * throws SOAPException if response is not XML.
   */
  public byte[] call (byte[] payload) 
    throws SOAPException,IOException;

  /**
   * Method to set the endpoint url.  The endpoint url is where the 
   * particular service resides.
   * @param endpoint String representation of the endpoint
   * @return void
   */
   public void setEndpoint(String endpoint);

  /**
   * Method to set the SOAPAction.  
   * @param soapAction String representation of the SOAPAction
   */
   public void setSOAPAction(String SOAPAction);

  /**
   * Method to get the endpoint url 
   * @return Endpoint URL
   */
   public String getEndpoint();

  /**
   * Method to get the SOAPAction
   * @return SOAPAction
   */
   public String getSOAPAction();
   
   /**
   * Method to specify if the SOAP payload
   * sent and received is to be printed out.
   * This is particularly useful when the wire
   * payload is necessary as in case of debugging
   * purposes.
   * @param debug true if the payload is to 
   * be displayed; false otherwise.
   */
   public void setDisplayPayload(boolean debug);
   
   /**
   * Please see documentation for displayPayload
   * @return 
   */
   public boolean isDisplayPayload();
}
