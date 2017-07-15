/*

 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.

 * Please see http://www.wingfoot.com for license details.

 */





package com.wingfoot.soap.transport;



import java.util.*;

import java.io.*;

import com.wingfoot.soap.*;

import java.net.*;



/**

 * Class to aid in making SOAP calls over HTTP using

 * J2SE.

 * @since 0.90

 * @author Kal Iyer

 */



public class J2SEHTTPTransport implements Transport 

{

  private String url;

  private boolean shouldGetResponse=true;

  private String soapAction;

  private Vector httpHeader;

  private boolean useSession=false;

  private String cookieName="JSESSIONID";

  private String cookieValue=null;
  private boolean displayPayload=false;
    

  /**

   * Creates an instance of HTTPTransport with the 

   * url and port number specified.  This class is

   * immutable.

   * @since 0.90

   */

  public J2SEHTTPTransport () 

  {

  } 

   /**
   * Method to specify if the SOAP payload
   * sent and received is to be printed out.
   * This is particularly useful when the wire
   * payload is necessary as in case of debugging
   * purposes.
   * @param debug true if the payload is to 
   * be displayed; false otherwise.
   */
   public void setDisplayPayload(boolean debug) 
   {
     this.displayPayload=debug;
   }
   
   /**
   * Please see documentation for displayPayload
   * @return 
   */
   public boolean isDisplayPayload() 
   {
     return displayPayload;
   }


  /**

   * Method to set the endpoint URL.  The endpoint url is used

   * to determine where a particualr service is located.

   * @param url The endpoint url

   */

  public void setEndpoint(String url)

  {

    this.url = url;

  }



  /**

   * Method to set the SOAPAction HTTP Header

   * @param soapAction The actual header value to set

   */

  public void setSOAPAction(String soapAction)

  {

    this.soapAction = soapAction != null? soapAction : "\"\"";

  }



  /**

   * Method which retrieves the endpoint 

   * @return String The endpoint url

   */

  public String getEndpoint()

  {

    return url;

  }



  /**

   * Method which retrieves the SOAPAction HTTP Header value

   * @return String the SOAPAction header

   */

  public String getSOAPAction()

  {

    return soapAction;

  }

  

  /**

   * Identifies if the session has to be maintained.

   * Since HTTP is a stateless protocol, cookies are

   * a popular way to maintain state across multiple

   * requests.  For J2EE based servers, the

   * JSESSIONID cookie is used to maintain state. For

   * other platforms, the cookie name might be different.

   * This method takes the cookieName that is sent back

   * to the server.

   * @param usage boolean that identifies if a session has

   * to be maintained; true indicates that the cookie

   * sent back by the server is sent back to the server

   * thereby maintaining state; false indicates the cookie

   * is not sent back; default is false

   * @param cookieName the name of the cookie that is used

   * to maintain the state; if null JSESSIONID is used.

   */

  public void useSession(boolean usage, String cookieName) 

  {

    this.useSession=usage;

    if (cookieName!=null) this.cookieName=cookieName;

  } /*useSession*/

  

  /**

   * Method to specify HTTP Header(s).  These headers

   * are  in addition to the following HTTP headers

   * always sent to the server:

   * Content-Language - always set to "en-US"

   * Content-Type - always set to "text/xml"

   * Content-Length - set to the length of the SOAP payload

   * SOAPAction - the SOAPAction specified in the constructor;

   * "" is a null was passed to the constructor.

   * @since 1.01 

   * @param name the name of the HTTP header; cannot be null

   * @param value the value of the header; cannot be null

   * @throws SOAPException if the key or value is null

   */

  public void addHeader(String name, String value) 

  throws SOAPException 

  {

    if (name==null || value==null)

      throw new SOAPException ("Header name and value cannot be null");

    if (httpHeader==null) httpHeader=new Vector();

    httpHeader.addElement (new String[] {name, value});

  } /* addHeader(string, string) */



    

    /**

     * Method to specify if the HTTP call should wait

     * for a response from the service.  If the boolean

     * is set to false, the XML payload is sent and

     * the response is not retrieved or parsed. Default

     * is true (i.e. retrieves response).

     * @since 0.90

     * @param shouldGetResponse true if a response is to

     * be retrieved (default); false if response is not

     * to be retrieved.

     */

  public void getResponse(boolean shouldGetResponse) 

  {

    this.shouldGetResponse = shouldGetResponse;

  }

    

  /**

   * Makes a HTTP connection the the URL and port specified.

   * @param payload the SOAP message as a byte array.

   * @return a byte array with the response from the server;

   * null if shouldGetResponse is false;

   * @throws IOException if any error occurs during connection

   * to the server.

   * @throws SOAPException if the response is not XML.

   */

  public byte[] call (byte[] payload) 

  throws SOAPException, IOException, MalformedURLException

  {

            

    OutputStream outputStream=null;

    BufferedOutputStream bos=null;

    InputStream inputStream=null;

    BufferedInputStream bis=null;

    ByteArrayOutputStream baos=null;

    HttpURLConnection uc=null;

    URL theURL=null;



    try 

    {

      theURL = new URL(this.url);

      uc = (HttpURLConnection)theURL.openConnection();

      uc.setDoOutput(true);

      uc.setDoInput(true);

      uc.setUseCaches(false);

      /**

       * Send data as POST to server.

       */

      uc.setRequestProperty ("Content-Language", "en-US");

      uc.setRequestProperty ("Content-Type", "text/xml");

      uc.setRequestProperty ("Connection", "close");

      uc.setRequestMethod("POST");

      uc.setRequestProperty ("Content-Length", 

			     Integer.toString (payload.length));

      uc.setRequestProperty ("SOAPAction", soapAction);



      /*session stuff added back in*/

      if (useSession && cookieValue!=null)

        uc.setRequestProperty("Cookie", cookieValue);

      

      if (httpHeader!=null) 

      {

        for (int i=0;i<httpHeader.size(); i++) 

        {

          String[] theHead=(String[]) httpHeader.elementAt(i);

          uc.setRequestProperty(theHead[0], theHead[1]);

        }

      } //if
      if (this.isDisplayPayload())
        System.err.println("**Sending:"+ new String(payload));

      outputStream = uc.getOutputStream();

      bos = new BufferedOutputStream(outputStream);

      bos.write(payload, 0, payload.length);

      bos.close();

      outputStream.close();

      outputStream=null;         

      /**

       * Retrieve the response from the  server

       */

      if (shouldGetResponse) 

      {

        baos=new ByteArrayOutputStream();

        try 

        {

          uc.connect();

          if (useSession) 

          {

             String tempSes=readCookie(uc);

             if (tempSes!=null && !tempSes.equals(cookieValue))

              cookieValue=tempSes;

          }

          inputStream = uc.getInputStream();

        } catch (Exception e) 

        {

          inputStream=uc.getErrorStream();

          if (inputStream==null) 

          {

            uc.disconnect();

            throw new IOException(e.getMessage());

          }

        }

        bis = new BufferedInputStream(inputStream);

        byte[] b = new byte[5250];

        while (true) 

        {

          int i = inputStream.read(b,0,5250);

          if (i== -1) break;

          baos.write(b,0,i);

        }

        b=baos.toByteArray();

        /**
         * Debug messages
         */
        if (this.isDisplayPayload())
        {
          System.err.println("*Received:" + new String(b));
          System.err.println("*Response Code:" + uc.getResponseCode());
        }
        return b;

      }

      else return null;

    } finally {

      if (uc!=null) uc.disconnect();

      if (bos != null) bos.close();

      if (outputStream != null) outputStream.close();

      if (bis != null) bis.close();

      if (inputStream != null) inputStream.close();

      if (baos != null) baos.close();

    } /* finally */

  } //call

  

  private String readCookie(HttpURLConnection conn) 

  throws IOException 

  {

    String key=null;

    String value=null;

    String[] substrs=null;

    for (int i=0; (key=conn.getHeaderFieldKey(i))!=null; i++) {

      if (key.toLowerCase().equals("set-cookie")) {

        value=conn.getHeaderField(i);

        while (value!=null) {

          substrs=split(value, ';');

          if (substrs!=null &&

              substrs[0].startsWith(cookieName))

            return substrs[0];

          value=substrs[1];

        } //while

      } //if

    } //for

    return null;

  } //readCookie



  private String[] split(String str, char ch) 

  {

    if (str==null)

      return null;

    String[] retVal = new String[] {null, null};

    int pos = str.indexOf(ch);

    if (pos!=-1)

    {

      retVal[0]=str.substring(0,pos).trim();

      retVal[1]=str.substring(pos+1).trim();

    } else

      retVal[0]=str.trim();

    return retVal;

  }

} /* com.wingfoot.soap.transport.HTTPTransport*/ 

