/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */
package com.wingfoot.soap.server;
import java.util.*;
import org.kxml.*;
import org.kxml.parser.*;
import java.io.*;
import java.net.*;
/**
 * Class that aids in deploying a service.  A service
 * can be deployed from command line or from a JSP 
 * front end.
 * The defination of the service is in the form of a
 * XML deployment descriptor.  The DTD of the service
 * is available at ?????
 * @author Kal Iyer
 */

public class DeploymentAdmin {
     
  private XmlParser parser;

  /**
   * Entry point for command line invocation of DeploymentAdmin
   * @param String[] - argv[0] represents the XML file name that
   * encapsulates the deployment descriptor.
   * argv[1] is the URL of the listener.
   * @throws Exception if any error occured while processing
   * the deployment deescriptor
   */

  public static void main (String[] argv) throws Exception {
         
    if (argv.length < 2) {
      System.err.println("Incorrect Arguments!!!");
      System.err.println("java com.wingfoot.soap.server.DeploymentAdmin listenerURL [deploy | undeploy | list] [ DescriptorFileName | Service to remove] ");

    } else if ((argv[1].trim().equalsIgnoreCase("deploy") ||
                argv[1].trim().equalsIgnoreCase("undeploy")) &&
                argv.length != 3) {

      System.err.println("Incorrect Arguments!!!");
      System.err.println("java com.wingfoot.soap.server.DeploymentAdmin listenerURL [deploy | undeploy | list] [ DescriptorFileName | Service to remove] ");

    } else if (argv[1].trim().equalsIgnoreCase("deploy")) {

      String retString = (new DeploymentAdmin()).processDDFile(argv[2], argv[0]);
      System.err.println(retString);
    } else if (argv[1].trim().equalsIgnoreCase("undeploy")) {

      String retString = (new DeploymentAdmin()).removeService(argv[2], argv[0]);
      System.err.println(retString);

    } else if (argv[1].trim().equalsIgnoreCase("list")) {

      Vector list =  (new DeploymentAdmin()).listService( argv[0]);
      for (int i=0; i<list.size(); i++) {
          System.err.println(list.elementAt(i)+"");
      }
    } 

  } /* main */
     
  /**
   * Removes a service.  Expects the name of the
   * URN that is to be removed and the URL of the
   * listener.  Throws an exception if any error
   * occurs while removing the service
   * @param serviceToRemove - the URN of the service
   * to remove.
   * @param listenerURL - the URL of the listener.
   * @param listenerURL - the URL of the listener.
   * @return String returned by the SOAP Listener
   */
  public String removeService(String serviceToRemove,
                            String listenerURL) throws IOException {

    Hashtable h=new Hashtable();
    h.put("op", "undeploy");
    h.put("serviceName", serviceToRemove);

    /* Don't catch exception, let calling program handle it */
    byte[] ret=this.contactListener(listenerURL, null, h);

    return new String (ret);

  } /*removeService*/

     
  /**
   * Lists out service.  Interfaces with the
   * listener to retrieve a list of service
   * names and displays them.
   * @return Vector of services
   */
  public Vector listService(String listenerURL) 
  throws IOException, ClassNotFoundException {

    ByteArrayInputStream bais = null;
    ObjectInputStream ois     = null;
    Hashtable h               = new Hashtable();
    h.put("op", "list");

    /* Don't catch exception, let calling program handle it */
    byte[] b                  = this.contactListener(listenerURL, null, h);

    Vector list               = null;

    try {
      if (b!=null) {
        bais = new ByteArrayInputStream(b);
        ois  = new ObjectInputStream(bais);

        list = (Vector) ois.readObject();
      }
    } finally {
      if (bais != null) bais.close();
      if (ois  != null) ois.close();
    }
    return list;
  } /* listService */


  /**
   * Method takes the xml file and parsers it.  It creates and
   * instance of Service.  
   * @param fileName the filename that has the DD.
   * @param listenerURL the URL of the listener.
   * @throws IOException if the filename is not found or there
   * is an error processing it.
   * @throws DeploymentException if the deployment descriptor is missing
   * required information.
   * @return String returned by the SOAP Listener
   */
  public String processDDFile (String fileName,
                             String listenerURL)
    throws IOException, DeploymentException {
    Service dd=null;

    parser = new XmlParser(new InputStreamReader(new FileInputStream(fileName)));

    String retString = "";

    while (parser.peek().getType() != Xml.END_DOCUMENT) {
                
      if (parser.peek().getType()==Xml.START_TAG) {
                     
        if (parser.peek().getName().trim().equals("service")) {
          dd=new Service();
          String[] tmpArray = parseService(parser);
          dd.setServiceName(tmpArray[0]);
          if(tmpArray[1].trim().equals("document"))
            dd.setIsDocumentStyle(true);
        }
        else if (parser.peek().getName().trim().equals("namespace")) {
          dd.setServiceNamespace(parseNamespace(parser));
        }
        else if (parser.peek().getName().trim().equals("classname")) {
          dd.setServiceClass(parseClassname(parser));
        }
        else if (parser.peek().getName().trim().equals("portType")) {
          String[] tmpArray = parsePortType(parser);
          dd.setPortTypeName(tmpArray[0]);
          dd.setPortTypeNamespace(tmpArray[1]);
        }

        else if (parser.peek().getName().trim().equals("method")) {
          dd.setMethod(parseMethod(parser));
        }
        else if (parser.peek().getName().trim().equals("typemap")) {
          dd.setTypemap(parseTypemap(parser));
        }
        else if (parser.peek().getName().trim().equals("elementmap")) {
          dd.setElementmap(parseElementmap(parser));
        }
        else if (parser.peek().getName().trim().equals("wsdl")) {
          dd.setWSDLName(parseWSDLName(parser));
        }
      }
      else if (parser.peek().getType()==Xml.END_TAG &&
               parser.peek().getName().trim().equals("service")) {
                     
        if (dd.getServiceName()==null)
          throw new DeploymentException("Service name cannot be null");
        else if (dd.getServiceNamespace()==null)
          throw new DeploymentException("namespace name cannot be null");
        else if (dd.getServiceClass()==null)
          throw new DeploymentException("classname cannot be null");
        else if (dd.getMethod()==null)
          throw new DeploymentException("Must have at least one method defined");
        else if (dd.getWSDLName()==null)
          throw new DeploymentException("Must contain the wsdl location");
        else {
          Hashtable h=new Hashtable();
          h.put("op", "deploy");

          /* Don't catch exception, let calling program handle it */
          retString = new String(this.contactListener(listenerURL, dd, h));
        }
        parser.read(); //skip past the </service> element
      }
                
      else {
        //Don't know what this is. Just skip it.
        parser.read();
      }
    } //while

    return retString;

  } /* processDDFile */
      
  /**
   * Checks to see if the attribute is null.
   * Returns true if the attribute is null,
   * false if not null.
   */
  private boolean isAttributeNull(Attribute theAttribute) {
    if (theAttribute==null ||
        theAttribute.getValue()==null ||
        theAttribute.getValue()=="" ||
        theAttribute.getValue().trim().length()==0)

      return true;
    else return false;
  }
      
  /**
   * Parses a element element.
   * @param parser instance of XmlParser that is pointing
   * to a typemap element.
   * @return String[] 
   * @throws IOException if the filename is not found or there
   * is an error processing it.
   * @throws DeploymentException if the deployment descriptor is missing
   * required information.
   */
  private String[] parseElementmap(XmlParser parser) throws
    IOException, DeploymentException {

    String[] returnValue=new String[4];
    returnValue[2]=null;
    while (true) {
      ParseEvent pe = parser.read();
      if (pe.getType()==Xml.END_TAG &&
          pe.getName().equals("elementmap")) {

        break;
      }

      else if (pe.getType()==Xml.START_TAG &&
               pe.getName().trim().equals("elementName")) {

        String body=getElementBody(parser,pe.getName().trim());
                     
        if (body==null || body.equals("") ||
            body.trim().length()==0) {
          throw new DeploymentException (
                                         "elementName element does not have a body");
        }
                     
        returnValue[0]=body;
      }

      else if (pe.getType()==Xml.START_TAG &&
               pe.getName().equals("type")) {

        String body=getElementBody(parser,pe.getName().trim());
                     
        if (body==null || body.equals("") ||
            body.trim().length()==0) {
          throw new DeploymentException (
                                         "type element in does not have a body");
        }
                     
        returnValue[1]=body;
      }
      else if (pe.getType()==Xml.START_TAG &&
               pe.getName().equals("XML2JavaClass")) {

        String body=getElementBody(parser,pe.getName().trim());
                     
        if (body==null || body.equals("") ||
            body.trim().length()==0) {
          returnValue[3]=null;
        }
        else
          returnValue[3]=body;
      }
      else if (pe.getType()==Xml.START_TAG &&
               pe.getName().equals("isArray")) {
        String body=getElementBody(parser,pe.getName().trim());
        if (body!=null && !body.equals("") ||
            body.trim().equalsIgnoreCase("true"))
          returnValue[2]="true";
      }
    } //while true

    if (returnValue[0]==null || returnValue[1]==null) {
      throw new DeploymentException (
                                     "elementmap does not have adequate information");
    }

    return returnValue;
  } /*parseElementmap*/ 

  /**
   * Parses a typemap element.
   * @param parser instance of XmlParser that is pointing
   * to a typemap element.
   * @return String[] 
   * @throws IOException if the filename is not found or there
   * is an error processing it.
   * @throws DeploymentException if the deployment descriptor is missing
   * required information.
   */
  private String[] parseTypemap(XmlParser parser) throws
    IOException, DeploymentException {

    String[] returnValue=new String[5];

    while (true) {
      ParseEvent pe = parser.read();
      if (pe.getType()==Xml.END_TAG &&
          pe.getName().equals("typemap")) {
        break;
      }
      else if (pe.getType()==Xml.START_TAG &&
               pe.getName().equals("typeNamespace")) {

        String body=getElementBody(parser,pe.getName().trim());
                     
        if (body==null || body.equals("") ||
            body.trim().length()==0) {
          throw new DeploymentException (
                                         "typeNamespace element does not have a body");
        }
                     
        returnValue[0]=body;
      }

      else if (pe.getType()==Xml.START_TAG &&
               pe.getName().equals("qname")) {

        String body=getElementBody(parser,pe.getName().trim());
                     
        if (body==null || body.equals("") ||
            body.trim().length()==0) {
          throw new DeploymentException (
                                         "qname element in does not have a body");
        }
                     
        returnValue[1]=body;
      }
      else if (pe.getType()==Xml.START_TAG &&
               pe.getName().equals("type")) {

        String body=getElementBody(parser,pe.getName().trim());
                     
        if (body==null || body.equals("") ||
            body.trim().length()==0) {
          throw new DeploymentException (
                                         "type element in does not have a body");
        }
                     
        returnValue[2]=body;
      }
      else if (pe.getType()==Xml.START_TAG &&
               pe.getName().equals("Java2XMLClass")) {

        String body=getElementBody(parser,pe.getName().trim());
                     
        if (body==null || body.equals("") ||
            body.trim().length()==0) {
          throw new DeploymentException (
                                         "Java2XMLClass element in does not have a body");
        }
                     
        returnValue[3]=body;
      }
      else if (pe.getType()==Xml.START_TAG &&
               pe.getName().equals("XML2JavaClass")) {

        String body=getElementBody(parser,pe.getName().trim());
                     
        if (body==null || body.equals("") ||
            body.trim().length()==0) {
          throw new DeploymentException (
                                         "XML2JavaClass element in does not have a body");
        }
                     
        returnValue[4]=body;
      }
    } //while

    if (returnValue[0]==null || returnValue[1]==null ||
        returnValue[2]==null || returnValue[3]==null ||
        returnValue[4]==null) {
      throw new DeploymentException (
                                     "typemap element does not have adequate information");
    }

    return returnValue;
  } /* parseTypemap */
      
  /**
   * Utility method.  The parser is pointing to a 
   * START_TAG with a body.  The method returns 
   * the body.
   */
  private String getElementBody(XmlParser parser,
                                String elementName) throws IOException {
    String elementBody=null;
    while (true) {
      ParseEvent pe=parser.read();
      if (pe.getType()==Xml.END_TAG &&
          pe.getName().trim().equals(elementName.trim())) {
        break;
      } 
      else if (pe.getType()==Xml.TEXT) {
        elementBody=pe.getText().trim();
      }
    } //while
    return elementBody;
  } /*getElementBody*/
      
  /**
   * Takes a parser that is pointing to the <serivce> element
   * and parsers it.  
   * @param parser an instance of XmlParser; reading the next
   * element returns the service element
   * @return String[] element[0] is the value of the name attribute of the service
   * element and element[1] is the value of the style attribute
   * @throws DeploymentException if the name attribute is not
   * present in the service element
   * @throws IOException if any error occurs while reading the
   * Xml descriptor.
   */
  private String[] parseService(XmlParser parser)
    throws IOException, DeploymentException {

    ParseEvent startTag=parser.read();
    Attribute theAttribute=startTag.getAttribute("name");
    Attribute styleAttribute=startTag.getAttribute("style");
    
    if (isAttributeNull(theAttribute))
      throw new DeploymentException("The element " + startTag.getName() +
                                    " does not have a name attribute");
    if (isAttributeNull(styleAttribute))
      throw new DeploymentException("The element " + startTag.getName() +
                                    " does not have a style attribute");
    
    return new String[] {theAttribute.getValue().trim(), styleAttribute.getValue().trim()};

  } /* parseService*/

  private String[] parsePortType(XmlParser parser)
    throws IOException, DeploymentException {

    ParseEvent startTag=parser.read();
    Attribute theAttribute=startTag.getAttribute("name");
    Attribute styleAttribute=startTag.getAttribute("namespace");
    
    if (isAttributeNull(theAttribute))
      throw new DeploymentException("The element " + startTag.getName() +
                                    " does not have a name attribute");
    if (isAttributeNull(styleAttribute))
      throw new DeploymentException("The element " + startTag.getName() +
                                    " does not have a namespace attribute");
    
    return new String[] {theAttribute.getValue().trim(), styleAttribute.getValue().trim()};

  } /* parseService*/

  /**
   * Takes a parser that is pointing to the <method> element
   * and parsers it.  The return will be a vector encapsulating the method name
   * as the first element and parameters as subsequent elements
   * @param parser an instance of XmlParser; reading the next
   * element returns the service element
   * @return Vector with methodName as the first element and parameters nested inside
   * a String[], with the first element as the parameter name and the second element
   * as the type
   * @throws DeploymentException if the name attribute is not
   * present in the service element
   * @throws IOException if any error occurs while reading the
   * Xml descriptor.
   */
  private Vector parseMethod (XmlParser parser)
    throws IOException, DeploymentException {

    Vector returnVector = new Vector();

    while (true) {

      ParseEvent pe = parser.read();

      if (pe.getType()==Xml.END_TAG &&
          pe.getName().trim().equals("method")) {
        break;
      }
      else if (pe.getType()==Xml.START_TAG &&
               pe.getName().equals("method")) {
       
        Attribute theAttribute=pe.getAttribute("name");

        if (isAttributeNull(theAttribute))
          throw new DeploymentException("The element " + pe.getName() +
                                    " does not have a name attribute");


        
        returnVector.add(theAttribute.getValue());
        
      } else if(pe.getType()==Xml.START_TAG &&
      pe.getName().trim().equals("parameter"))
      {
        String[] tmpArray = new String[2];
        Attribute typeAttribute=pe.getAttribute("type");

        if (isAttributeNull(typeAttribute))
          throw new DeploymentException("The element " + pe.getName() +
                                    " does not have a type attribute");
                                    
        tmpArray[0] = pe.getAttribute("name").getValue();
        tmpArray[1] = typeAttribute.getValue();
        returnVector.add(tmpArray);

      }
      
    } //while true

    return returnVector;

  } /* parseMethod*/

  /**
   * Takes a parser that is pointing to the <namespace> element
   * and parsers it.  
   * @param parser an instance of XmlParser; reading the next
   * element returns the namespace element
   * @return String the body of the namespace element
   * @throws DeploymentException if there is not body in the 
   * namespace element.
   * @throws IOException if any error occurs while reading the
   * Xml descriptor.
   */
  private String parseNamespace(XmlParser parser) 
    throws IOException, DeploymentException {

    String returnValue=null;
    while (true) {

      ParseEvent pe = parser.read();

      if (pe.getType()==Xml.END_TAG &&
          pe.getName().equals("namespace"))
        break;

      else if (pe.getType()==Xml.TEXT)
        returnValue=pe.getText().trim();
    } //while true

    if (returnValue==null ||
        returnValue.equals("") ||
        returnValue.trim().length()==0)
      throw new DeploymentException
        ("The namespace element does not have a namespace");

    return returnValue;
  } /* parseNamespace */

  /**
   * Takes the parser that is pointing at the <wsdl> element and parses it
   * @param parser an instance of XmlParser
   * @return String representation of the WSDLName
   * @throws IOException if any error occurs while reading the Xml descriptor
   * @throws DeploymentException if any error occurs during the parsing of the descriptor
   */
  private String parseWSDLName(XmlParser parser)
  throws IOException, DeploymentException
  {

    ParseEvent startTag=parser.read();
    Attribute theAttribute=startTag.getAttribute("name");
           
    if (isAttributeNull(theAttribute))
      throw new DeploymentException("The element" + startTag.getName() +
                                    " does not have a location attribute");
    return theAttribute.getValue().trim();

  }

  /**
   * Returns the parameter name encapsulated in the <parameter> element.  
   * @param parser an instance of the XmlParser with the pointer at the correct location
   * @return String parameter name
   * @throws IOException if any error occurs while reading the Xml descriptor
   * @throws DeploymentException if any erro occurs while retrieving the parameter name
   */
  private String parseParameter(XmlParser parser)
  throws IOException, DeploymentException
  {
    ParseEvent startTag = parser.read();
    Attribute theAttribute = startTag.getAttribute("name");

    if(isAttributeNull(theAttribute))
      throw new DeploymentException("The element" + startTag.getName() +
                                    " does not have a name attribute");

    return theAttribute.getValue().trim();
  }
  
  /**
   * Takes a parser that is pointing to the <classname> element
   * and parsers it.  
   * @param parser an instance of XmlParser; reading the next
   * element returns the namespace element
   * @return String the body of the classname element
   * @throws DeploymentException there is not body in the classname
   * element.
   * @throws IOException if any error occurs while reading the
   * Xml descriptor.
   */
  private String parseClassname(XmlParser parser) 
    throws IOException, DeploymentException {

    String returnValue=null;
    while (true) {

      ParseEvent pe = parser.read();

      if (pe.getType()==Xml.END_TAG &&
          pe.getName().equals("classname"))
        break;

      else if (pe.getType()==Xml.TEXT)
        returnValue=pe.getText().trim();
    }

    if (returnValue==null ||
        returnValue.equals("") ||
        returnValue.trim().length()==0)
      throw new DeploymentException
        ("The classname element does not have a value");

    return returnValue;
  } /* parseNamespace */

  /**
   * Deploys the service.  The method has enough parameters
   * to deploy the service. Creates an instance of the
   * Service and sends the object to the 
   * listener specified in the first argument.
   * @param listenerURL the URL of the listener
   * @param serviceName the name of the service
   * @param className the name of the Java class being deployed
   * @param classNamespace the namespace associated with the class
   * being deployed
   * @param method Vector containing array of Strings (String[])
   * Each element of the String[] contains the following
   * <ul>
   * <li> 0 - true or false (static or not)
   * <li> 1 - return parameter name or null if none specified
   * <li> 2 - the method name
   * </ul>
   * @param typemap Vector containing array of Strings (Strng[])
   * Each element of the String[] contains the following
   * <ul>
   * <li> 0 - the namespace
   * <li> 1 - the qname
   * <li> 2 - Java class that is being mapped.
   * <li> 3 - Java2XMLClass
   * <li> 4 - XML2JavaClass
   * </ul>
   * @param elementmap Vector containing array of Strings (Strng[])
   * Each element of the String[] contains the following
   * <ul>
   * <li> 0 - the name of the element
   * <li> 1 - Java class that is being mapped
   * <li> 2 - XML2JavaClass; this is optional.
   */
  void deployService( String listenerURL,
                      String serviceName,
                      String className,
                      String classNamespace,
                      Vector method,
                      Vector typemap,
                      Vector elementmap) {

  } /* deployService */
      
  /**
   * Interfaces with the listener to deploy, list
   * or remove a Service.
   * @param listenerURL - the URL of the listener
   * @param service instance of Service.  Depending
   * on the operation, this could be null.
   * @param operation Hashtable of name value
   * pairs that contains the HTTP parameters
   * The most important parameter here is op 
   * that indicates the kind of operation the
   * listeners wants to invoke on the Router.
   * The possible values for op are:
   * <ul>
   * <li> deploy - deploys a service encapsulated
   * in the Service object
   * <li> remove - removes a service
   * <li> list - list all the services available.
   * @throws IOException if any error occurs during
   * converting Service to a byte array.
   */
  private byte[] contactListener(String listenerURL, 
                                 Service service,
                                 Hashtable operation) 
    throws IOException {

    ByteArrayOutputStream bos=null;
    ObjectOutputStream oos=null;
    OutputStream os=null;
    InputStream inputStream=null;
    BufferedOutputStream bs=null;
    BufferedInputStream bis=null;
    URL theURL=null;
    HttpURLConnection uc=null;
    boolean isError=false;
    try {
           
      /**
       * Take the Service and converts it to a byte array.
       */
      byte[] theByteArray=null;
      if (service!=null) {
        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        oos.writeObject(service);
        theByteArray = bos.toByteArray();
        oos.close(); bos.close();
      }

      // Construct the URL
      if (operation!=null) {
        listenerURL+="?";
        int ctr=0;
        Enumeration e = operation.keys();
        while (e.hasMoreElements()) {
          if (ctr!=0) 
            listenerURL+="&";
          ctr++;
          String name=(String)e.nextElement();
          String value=(String)operation.get(name);
          listenerURL+=name+"="+value;
        }
      } //if
      //Make the actual URL connection
      theURL = new URL(listenerURL);
      uc=(HttpURLConnection) theURL.openConnection();
      uc.setUseCaches(false);
      uc.setDoOutput(true);  //becomes a POST
      uc.setRequestMethod("POST");
      uc.setRequestProperty("Content-Type",
                            "application/octet-stream");
      if (theByteArray!=null) {
        uc.setRequestProperty("Content-Length",
                              theByteArray.length+"");
        os=uc.getOutputStream();
        bs=new BufferedOutputStream(os);
        bs.write(theByteArray, 0, theByteArray.length);
        bs.flush();
        //bs.close(); os.close();
      }

      // Get the response
      theByteArray=null;
      try {
        uc.connect();
        inputStream = uc.getInputStream();
      } catch (Exception e) {
        inputStream=uc.getErrorStream();
        isError=true;
        if (inputStream==null) {
          uc.disconnect();
          throw new IOException(e.getMessage());
        }
      }
      bis=new BufferedInputStream(inputStream);
      bos=new ByteArrayOutputStream();
      byte[] b=new byte[250];
      while (true) {
        int i=bis.read(b,0,250);
        if (i==-1) break;
        bos.write(b,0,i);
      }
      if (isError) {
        System.err.println(new String(bos.toByteArray()));
        return null;
      }
      else return bos.toByteArray();
    } finally {
      if (bos !=null) bos.close();
      if (oos!=null) oos.close();
      if (os!=null) os.close();
      if (inputStream!=null) inputStream.close();
      if (bs!=null) bs.close();
      if (bis!=null) bis.close();
      if (uc !=null) uc.disconnect();
    }
  } // contactListener
} /* com.wingfoot.soap.server.DeploymentAdmin */
