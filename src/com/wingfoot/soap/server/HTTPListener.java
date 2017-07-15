/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */

package com.wingfoot.soap.server;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.wingfoot.soap.*;
import com.wingfoot.*;
/**
 * The HTTPListener is the entry point for all 
 * HTTP based SOAP requests. The primary task of 
 * the listener is to pass requests on to the 
 * SOAPRouter, which will process it.  The listener 
 * then determines if the processed request was valid 
 * or if there is an error. The HTTP Code 200 is set
 * for  success and HTTP Code 500 for errors.
 * The HTTPListener is also responsible for initializing 
 * the Services that had been previously deployed.
 * Upon startup, the HTTPListener makes a call to the 
 * SOAPRouter to read the services deployed from 
 * persistent storage.
 * @author Baldwin Louie
 */

public class HTTPListener extends HttpServlet {

  private final boolean debug = true;
  SOAPRouter sr = null;

  /**
   * This initialization method does one important thing besides
   * the standard init's.
   * It will instantiate a SOAPRouter and try to load the services from the
   * file system.
   * @param ServletConfig
   * @throws ServletException
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    try{
      sr = new SOAPRouter();
      sr.loadService(); 
    } catch (Exception e){
      System.err.println(e.getMessage());
      e.printStackTrace();
      System.exit(-1);
    }
  }
     
  /**
   * Handles the HTTP Get requests.  The only
   * GET request supported is a simple operation
   * to check if the SOAP server is up and listening
   * for requests.  This is done by passing the 
   * following name-value pair: op=ok
   */
  public void doGet(HttpServletRequest req,   HttpServletResponse res)
    throws ServletException,IOException {
        
    String op = req.getParameter("op");

     try {
      writeResponse(res, "text/html", 
                    HttpServletResponse.SC_OK,
                    ("<html><body>Parvus Web Service Infrastructure (" + Constants.VERSION + ") listening.<br>"+Constants.BUILD+"</body></html>").getBytes());
                    
    } catch (Exception e){
      e.printStackTrace();
      debugMsg(e.getMessage());
    }
  } /* doGet */
    
  /**
   * Contains all the operations supported by the SOAPServer.
   * <ul>
   * <li>op=deploy - deploy a new service to the server
   * <li>op=undeploy - remove a service from the server
   * <li>op=list - return a list of the services avabile on the server
   * <li>a request without an operation signals a soap request, at which the body
   * of the request will be sent over to the SOAPRouter to be processed
   * </ul>
   * @param HttpServletRequest - the servlet request
   * @param HTtpServletResponse - the servlet response
   * @throws ServletException - throws this error if there are any servlet exceptions
   * @throws IOException - throws this if there is another IOException
   */
  public void doPost(HttpServletRequest req, 
		     HttpServletResponse res)
    throws ServletException, IOException {
        
    /**
     * Debug begins
	 

     Enumeration enum = req.getHeaderNames();
     while (enum.hasMoreElements()) {
     String name=(String) enum.nextElement();
     String value=(String) req.getHeader(name);
     System.err.println("Header: " + name + ":"+ value);
     }
     HttpSession hs = req.getSession(true);
     Integer theVal = (Integer) hs.getAttribute("testAttr");
     if (theVal==null) {
     hs.setAttribute("testAttr", new Integer(1));
     System.err.println("1");
     }
     else {
     theVal = new Integer(theVal.intValue()+1);
     System.err.println(" " + theVal);
     hs.removeAttribute("testAttr");
     hs.setAttribute("testAttr", theVal);
     }

	
     * Debug ends
     */

    String op = req.getParameter("op");
    /**
     * Deploy operation will add a service to the SOAP server.
     * When a deploy operation is finished, it will return a 
     * simple OK to the admin.
     * If an error occurs, an error message will be sent back to the admin
     */
    try {
       if (op == null) 
      { //message body detected
      /**
       * This will process the soap request
       */
      byte[] b = getBody(req.getInputStream(), 
             req.getContentLength());
      Object[] o = sr.execute(b, req.getRequestURI(), getWSDLPath(getServletConfig()));
      if(((String)o[0]).trim().equals("ok"))
        writeResponse(res, 
          "text/xml; charset=\"utf-8\"",
          HttpServletResponse.SC_OK, 
          (byte[])o[1]);
      else 
        writeResponse(res,
          "text/xml; charset=\"utf-8\"",
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
          (byte[])o[1]);
      } 
      else if(op.equals("publish"))
      {
        /**
         * This operation publishes a wsdl to the destination location
         * The wsdl comes in as a byte array.  We simply write it to disk.
         * The wsdl location is hard-coded to $PARVUS_WEBAPP_DIRECTORY/wsdl
         */
          byte[] b = getBody(req.getInputStream(), req.getContentLength());

          String[] wsdlArray = sr.publishWSDL(b,getWSDLPath(getServletConfig()));
          if(wsdlArray == null)
          {
            writeResponse(res,
                          "text/xml; charset=\"utf-8\"",
                          HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                          "No WSDLs were written to Parvus".getBytes());
          }
          writeResponse(res, 
                        "text/xml; charset=\"utf-8\"", 
                        HttpServletResponse.SC_OK, 
                        constructWSDLUrls(wsdlArray,req).getBytes());
      }
      else if(op.equals("deploy")) {

        //add the service
        byte[] b = getBody(req.getInputStream(), req.getContentLength());
        sr.addService(b);
        writeResponse(res,
                      "text/xml; charset=\"utf-8\"",
                      HttpServletResponse.SC_OK, 
                      "OK".getBytes());
      }

      else if(op.equals("undeploy")) {
      /**
       * Undeploy will remove a service from the SOAP server.
       * When an undeploy is finished, it will return a simple 
       * OK to the admin. If an error occurs, an error message 
       * will be sent back to the admin
       */
      sr.removeService(req.getParameter("serviceName"));
      writeResponse(res,
                    "text/xml; charset=\"utf-8\"",
                    HttpServletResponse.SC_OK, 
                    "OK".getBytes());
      }

      else if(op.equals("list")){
      /**
       * List retrieves information about what services are a
       * vailable on the SOAP Server.
       * If an error occurs, an error message is sent back to 
       * the admin
       */
      byte[] b = sr.listService();
      writeResponse(res,
                    "text/xml; charset=\"utf-8\"",
                    HttpServletResponse.SC_OK, 
                    b);
      } 

    } catch (Exception e){
      e.printStackTrace();
      debugMsg(e.getMessage());
      writeResponse(res,
		    "text/xml; charset=\"utf-8\"",
		    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
		    e.getMessage()==null? "null".getBytes():
		    e.getMessage().getBytes());		
    }

  } /* doPost */

  /**
   * Extracts the request body from the request.
   * Returns the body in the form of a byte[]
   * @param InputStream - the inputStream
   * @param int - the length of the request
   * @return byte[] - the byte[] extracted from the inputStream
   * @throws IOException 
   */
  private byte[] getBody(InputStream is, int contentLength) 
    throws IOException {

    BufferedInputStream bis=new BufferedInputStream(is);
    byte[] body = new byte[contentLength];
    int bLen = contentLength;
    while (bLen > 0){
      int i = bis.read(body, contentLength - bLen, bLen);
      bLen -= i;
             
    }
    bis.close();
    //is.close();
    return body;
  } /*getBody*/

  /**
   * This method write a response to the outputstream.  WriteResponse takes
   * the HttpServletResponse in order to set the neccessary header information
   * The boolean will signal whether this method should set an Error code or an Ok code
   * byte[] contains the message to be written to the outputstream
   * @param HttpServletResponse - the httpResponse so the neccessary header information
   *                              gets set
   * @param String - contentType indicates what type is being returned
   * @param int - responseCode indicates the response code
   * @param byte[] - the content to be written to the Output Stream
   @ @param throws IOException 
  */
  private void writeResponse(HttpServletResponse res,
			     String contentType,
			     int responseCode,
			     byte[] rByte) throws IOException {

    res.setContentType(contentType);
    res.setContentLength(rByte.length);
    res.setStatus(responseCode);
    BufferedOutputStream sos = new BufferedOutputStream(
							res.getOutputStream());
    sos.write(rByte,0, rByte.length);
    sos.close();
  } /*writeResponse*/

  /**
   * Helper method to get the path to the wsdl directory
   * @param sc the ServletConfig
   * @return String the path to the server
   */
  private String getWSDLPath(ServletConfig sc)
  throws Exception
  {
    String path = sc.getServletContext().getRealPath("/wsdl");
    if(path == null)
      throw new Exception("Server cannot determine wsdl file path");
    return path;
  }
  
  
  /**
   * Private helper method to format a message that is returned to the user
   * after publishing the wsdl files.  The message contains the url to the 
   * published wsdl.
   */
  private String constructWSDLUrls(String[] wsdlArray, HttpServletRequest req)
  throws Exception
  {
    StringBuffer rtnBuffer = new StringBuffer();
    if(wsdlArray == null)
      throw new Exception("No WSDLs were written to the Parvus Server");
      
    StringBuffer sb = new StringBuffer();
    sb.append(req.getScheme()).append("://").append(req.getServerName());
    if(req.getServerPort() != 80)
      sb.append(":").append(req.getServerPort());
    sb.append(req.getContextPath()).append("/").append("wsdl").append("/");
    
    for(int i = 0; i < wsdlArray.length; i++)
    {
      rtnBuffer.append(sb.toString()).append(wsdlArray[i]).append("\n");  
    }
    
    return rtnBuffer.toString();
  } //constructWSDLUrls

  private void debugMsg(String msg){
    if(debug)
      System.err.println("HTTPListener: " + msg);
  }


} /* com.wingfoot.soap.server.HTTPListener */
