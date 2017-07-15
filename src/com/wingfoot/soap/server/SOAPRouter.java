/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */
 /*
        if (mustUnderstandExists(envelope)) 
        {
          throw this.createRouterException(
             "Unable to process a mandatory Header",
             Fault.MUSTUNDERSTAND);
        }
        */
package com.wingfoot.soap.server;

import java.util.*;
import java.io.*;
import org.kxml.parser.*;
import org.kxml.*;
import org.kxml.io.*;
import com.wingfoot.soap.*;
import com.wingfoot.xml.*;
import com.wingfoot.soap.encoding.*;
import com.wingfoot.wsdl.*;
import com.wingfoot.wsdl.soap.*;
import java.lang.reflect.*;
import com.wingfoot.*;
/**
 * A SOAPRouter is primarily responsible to take a 
 * binary request from the listener, parse the request
 * and invoke the appropriate service.  To determine
 * the service to invoke, it maintains a memory and
 * persistent representation of the DeploymentDescriptor.
 * 
 * To add, remove or list the services registered with the
 * SOAP server, the DeploymentAdmin sends the request to
 * the listener.  The listener, in turn, makes the request
 * to the SOAPRouter.  The router is responsible to maintain
 * the representation of the DeploymentDescriptor.
 * @author Baldwin Louie
 * @modified Kal Iyer
 */

public class SOAPRouter {
    
  private static String deploymentFileName;

  /*public static*/ private Hashtable deploymentHashtable = new Hashtable();

  private static Hashtable wsdlHashtable = new Hashtable();
  private static Properties theProperties;

  private final int INPUT_MESSAGE = 0;
  private final int OUTPUT_MESSAGE = 1;
  private Hashtable primitiveMap;

  /**
   * Default construtor for the SOAPRouter. The
   * access level for the constructor is default
   * since only classes in com.wingfoot.soap.server
   * can access the SOAPRouter
   */
  SOAPRouter()
  {
    primitiveMap=new Hashtable();
    primitiveMap.put("byte", byte.class);
    primitiveMap.put("short", short.class);
    primitiveMap.put("int", int.class);
    primitiveMap.put("integer", int.class);
    primitiveMap.put("long", long.class);
    primitiveMap.put("float", float.class);
    primitiveMap.put("double", double.class);
    primitiveMap.put("boolean", boolean.class);
  } //constructor
    
    /**
     * Looks at the properties defined in 
     * wingfoot.properties (the properties file
     * for Wingfoot SOAP server).  This method 
     * takes the property name and returns the
     * property value; returns null if the 
     * property is not defined.
     * @param property the property name
     * @return String the property value.
     */
  public String getProperty(String property) {
    if (theProperties==null)
      return null;
    return theProperties.getProperty(property);
  }

  /**
   * Reads information
   * about the services previously deployed in the server
   * from persistent storage into transient storage (memory). 
   * Each service is encapsulated in the Service class.
   * When a client sends a SOAP payload to the listener, the 
   * lisetner forwards the payload to the SOAPRouter.  The
   * SOAPRouter parses the payload, uses the memory representation
   * of the Services to determine the service to invoke.
   * If the persistent storage is not accessible, then a new
   * transient representation is created.
   * @return boolean true if the services are successfully
   * read from persistent storage to transient storage, false
   * if not.
   * @throws RouterException if there is any error while reading 
   * from persistent storage.
   */
  void loadService() throws RouterException {
         

    /**
     * First check to see if the file exists.  If it does
     * not exist, create an null transient representation.
     */
    synchronized (deploymentHashtable) {
      try 
      {
      //Determine the deploymentfile name.  This file
      // has the services previously deployed.
      InputStream is =this.getClass().getResourceAsStream("/wingfoot.properties");
        if (is==null)
          throw new RouterException("Unable to open properties file:wingfoot.properties");
        
        theProperties = new Properties();
        theProperties.load(is);
        deploymentFileName=theProperties.getProperty("deploymentFile");

        File theFile = new File(deploymentFileName);

        if ( theFile.exists()) 
        {

          //file does exists; load it
          try 
          {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(deploymentFileName));
            deploymentHashtable = (Hashtable) ois.readObject();
            ois.close();

          } catch (Exception e) 
          {
            throw new RouterException
              ("Unusual error while reading "+
               deploymentFileName + " " + e.getMessage());
          }
        }//if
      } //outer try
      catch (SecurityException se) {
        throw new RouterException("Security violation while accessing " 
                + deploymentFileName);
      }
      catch (IOException se) {
        throw new RouterException("Error accessing wingfoot.properties " 
                + se.getMessage());

      }
    } //synchronized   
  } /* loadService */

    /**
     * Adds a Service to the SOAP server.  If the service already
     * exists, it is replaced.  
     * @param byte[] - serviceToDeploy byte representation of Service
     * @throws RouterException if the any fatal error is encountered.
     */
    
  void addService(byte[] serviceToDeploy) 
    throws RouterException {

    try 
    {
      ObjectInputStream ois = new ObjectInputStream(
						    new ByteArrayInputStream(serviceToDeploy));

      Service dd = (Service) ois.readObject();
      String serviceURNToStore = dd.isDocumentStyle()? dd.getServiceClass(): dd.getServiceNamespace();

      synchronized (deploymentHashtable) 
      {
        //This should never happen.
        if(deploymentHashtable == null)
          throw new RouterException
            ("Fatal error. Cannot initialize registry");
		
        //remove the current object
        if(deploymentHashtable.containsKey(serviceURNToStore)) 
          deploymentHashtable.remove(serviceURNToStore);
		
        deploymentHashtable.put(serviceURNToStore, dd);
		
        ObjectOutputStream oos = new ObjectOutputStream
          (new FileOutputStream(deploymentFileName));
        oos.writeObject(deploymentHashtable);
        oos.close();

      } //synchronized
		
    } catch (Exception e) {
      e.printStackTrace();
      throw new RouterException( e.getMessage());
    } 

  } /* addService */
    
    /**
     * Method to list the contents of the 
     * deployment hashtable.  This service is
     * typically requested by the deployment
     * admin.
     * @return byte[] - the byte array contains
     * a Vector of String with the service URN.
     * @throws RouterException if any error occurs
     * while retrieving the list.
     */
  byte[] listService() throws RouterException {
    Vector returnValue=new Vector();
    ObjectOutputStream oos=null;
    ByteArrayOutputStream bos=null;
    try {
      synchronized (deploymentHashtable) 
      {
      if (deploymentHashtable==null)
        throw new RouterException("Fatal error. Cannot initialize registry");
                
        //For the first cut, just send back the
        //namespace.  Since this is a vector, many
        // more things can be added later
        Enumeration e = deploymentHashtable.keys();
        while (e.hasMoreElements()) 
        {
          returnValue.add(e.nextElement());
        } //while
      }
      bos=new ByteArrayOutputStream();
      oos=new ObjectOutputStream(bos);
      oos.writeObject(returnValue);
      return bos.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
      throw createRouterException(e.getMessage(),
				  Fault.SERVER);
    } finally {
      try 
      {
        if (oos!=null) oos.close();
        if (bos!=null) bos.close();
      } catch (Exception e) 
      {
        throw createRouterException(e.getMessage(),
                                    Fault.SERVER);
      }
    }
  } /* listService */
     
  /**
   * Removes a service from the deployment table.
   * Once removed, it also write the new hashtable
   * to the persistent storage.
   * @param serviceURN - the name of the service that
   * has to be removed.
   * @throws RouterException if any error occurs while
   * removing the service.
   */
  void removeService(String serviceURN) throws RouterException {
    FileOutputStream fos=null;
    ObjectOutputStream oos=null;
    try {
      synchronized (deploymentHashtable) 
      {
        if (deploymentHashtable==null)
          throw new RouterException("Fatal error. Cannot initialize registry");
                   
        deploymentHashtable.remove(serviceURN);

        fos=new FileOutputStream(deploymentFileName);
        oos = new ObjectOutputStream(fos);
        oos.writeObject(deploymentHashtable);
      } 
    } catch (Exception e) {
      throw createRouterException(e.getMessage(),
				  Fault.SERVER);
    } finally {
      try {
        if (fos!=null) fos.close();
        if (oos!=null) oos.close();
      } catch (Exception e) {
        throw createRouterException(e.getMessage(),
                  Fault.SERVER);
      }

    } //finally
  } /*removeService*/

  /**
   * This method publishes a WSDL to a certain directory
   * The directory is hard-coded to $PARVUS_SERVER/wsdl
   * The return will be a String[] with all the wsdl names
   * that were written to the disk.  A String array is returned 
   * because in the future we might have the capability to publish
   * more than one wsdl at any given time.  
   * @param serializedHolder - this contains all the wsdl object to be written 
   * to disk
   * @param publishDirectory - determines which directory on the filesystem to 
   * publish this wsdl to.  This is hard-coded to $PARVUS_SERVER/wsdl
   * @return String[] contains the names of all the wsdls written to the filesystem
   */
  String[] publishWSDL(byte[] serializedHolder, String publishDirectory)
  throws RouterException, IOException, Exception
  {
    String[] wsdlArray = null;
    if(publishDirectory == null)
      throw new RouterException("Error: Cannot determine directory to publish WSDL");
      
    FileOutputStream fos = null;
    
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serializedHolder));
    SerializedHolder[] sh = (SerializedHolder[]) ois.readObject();
    wsdlArray = new String[sh.length];
    for(int i = 0; sh!=null&&i<sh.length; i++)
    {
      SerializedHolder s = sh[i];
      OutputStream os = new BufferedOutputStream(new FileOutputStream(publishDirectory+"/"+s.getDestination()));
      os.write(s.getXML());
      os.close();
      wsdlArray[i] = s.getDestination();
    }
    return wsdlArray;
  }
  
  /**
   * Called by the listener to invoke a service and
   * get back a response from the service.  The listener
   * sends the SOAP payload.  The router interfaces with
   * the parser to deserialize the payload.  The transient
   * registry is now scanned to determine the service to
   * invoke.  The response from the service is returned
   * back as a SOAP XML.
   * If any exception is thrown, a SOAP Fault element is
   * generated in the SOAP Body and the XML is returned.
   * @param payload the payload send by the listener.  The
   * listener receives this from the client.
   * @param uri the endpoint of the service invoked.  This determines
   * whether the service invoked is document or rpc
   * @param wsdlPath the path to the wsdl directory
   * @throws Exception if an unknown and unrecoverable error
   * occurs.
   * @return Object[] - index 0 contains String with ok or error. 
   * If error, the listener sets the HTTP error code to 500 as
   * the SOAP payload indicates a Fault. Index 1 contains 
   * byte[] which is the actual SOAP response that the  
   * listener sends back to the client.
   */
  Object[] execute(byte[] payload, String uri, String wsdlPath) throws Exception {
    Object[] returnValue=new Object[2];
    byte[] soapResponse=null;
    String methodName=null, targetURI=null, firstParameterName=null;
    //LiteralEnvelope le = null;
    Object[] theArguments=null;
    Class[] argumentClass=null;
    Service service =  null;
    QName portTypeQName = null;
    LiteralEnvelope le=null;
    WSDLHolder holder=null;
    TypeMappingRegistry tmr=null;
    boolean isPayloadDocumentStyle=false;

    try {
      /**
       * Log if payload received.  The logMessage
       * will log only if log is turned on.
       */
      logMessage(("Payload Received: " + new String(payload)).getBytes());

      String[] iParseArray = initialParse(payload);
      /**
       * this if/else checks to see if payload is document style.
       * If document style, the namespace that maps to the class
       * is retrieved from the URL; for RPC it is retrieved from
       * the wrapper that wraps the parameters.
       */
      if(isPayloadDocumentStyle(uri)) 
      {
        isPayloadDocumentStyle=true;
        service = (Service) deploymentHashtable.get(uri.substring(uri.lastIndexOf("/")+1));
        firstParameterName=iParseArray[0];
        if(service == null) {
          throw this.createRouterException(
					 "Unable to retrieve service mapped to "+iParseArray[0],
					 Fault.SERVER);
        }
        methodName = getMethodFromServiceObject(iParseArray==null?null:iParseArray[0],service);

        if(methodName == null)
          throw this.createRouterException("Unable to retrieve method stored in the service", Fault.SERVER);
      } //if payload is document style
      else
      {
        isPayloadDocumentStyle=false;
        //if payload is RPC
        service = (Service) deploymentHashtable.get(iParseArray[1]);
        if(service == null) 
        {
          throw this.createRouterException(
					 "Unable to retrieve service mapped to "+iParseArray[1],
					 Fault.SERVER);
        }
        methodName = iParseArray[0];
        targetURI = iParseArray[1];
        firstParameterName=iParseArray[2];
      } //if payload is RPC

      /**
       * Invoke the method only if the method is exposed as a
       * service.  If not, throw a RouterException
       */
      if (! service.isMethodExposed(methodName)) 
      {
        throw this.createRouterException("Method " +methodName +" is not exposed as a service",
         Fault.SERVER);
      }
      
      /**
       * Get the WSDLHolder.  The WSDLHolder encapsulates
       * a WSDL.
       */
      holder = getWSDLHolder(wsdlPath+"/"+service.getWSDLName());
      if(holder == null) 
      {
        throw this.createRouterException("Unable to retrieve WSDL stored in the Service", Fault.SERVER);
      }
      // Create TypeMappingRegistry with data from Service
      // It is used during deserialization.
      tmr = getTypeMappingRegistry(
			service.getTypemap(), service.getElementmap());

      /**
       * Create the envelope.
       */
      portTypeQName = new QName(service.getPortTypeNamespace(), service.getPortTypeName());
      PortType pType = holder.getPortType(portTypeQName);
      if(pType == null)
        throw this.createRouterException("Cannot retrieve the portType from the stored WSDL", Fault.SERVER);
      Operation operation = pType.getOperation(methodName,firstParameterName);
      if(operation == null)
        throw this.createRouterException("Cannot retrieve the operation from the stored WSDL", Fault.SERVER);
      Message message = operation.getInputMessage();
      if(message == null)
        throw this.createRouterException("Cannot retrieve the message from the stored WSDL", Fault.SERVER);
        
      le = new LiteralEnvelope(holder);
      le.setPortType(pType);
      le.setOperation(operation);
      le.setMessage(message);
      //Create the parser.
      XmlParser parser = new XmlParser(new InputStreamReader(new ByteArrayInputStream(payload),"UTF-8"));
      SOAPDeserializer aDeserializer=null;
      if(isUseLiteral(holder, methodName, INPUT_MESSAGE, portTypeQName)) 
        aDeserializer=new LiteralDeserializer(le,tmr,parser);
      else
        aDeserializer=new SectionVDeserializer(le,tmr,parser,payload);
      //Convert XML to Java objects.
      aDeserializer.deserialize();
      
      /**
       * Retrieve all the parameters from the envelope.
       * Store these parameters as an Object[].  It
       * is used to dynamically invoke the method.
       */
      if (le.getParameterCount() >0) 
      {
        theArguments = new Object[le.getParameterCount()];
        argumentClass = new Class[le.getParameterCount()];

        Vector argTypeVector = service.getMethod(methodName,isPayloadDocumentStyle?firstParameterName:iParseArray[2]);
        if (argTypeVector == null)
          throw this.createRouterException("Cannot retrieve the method from service object", Fault.SERVER);

        for (int i=1; i<argTypeVector.size(); i++)
        {
          String[] tmp = (String[]) argTypeVector.elementAt(i);
          if (primitiveMap.get(tmp[1])!=null)
            argumentClass[i-1] = (Class)primitiveMap.get(tmp[1]);
          else
          argumentClass[i-1] = Class.forName(tmp[1]);
          theArguments[i-1]=le.getParameter(i-1);
        }    
        //} //else
      }
      Object[] result=invokeService(service.getServiceClass(), //classname
            methodName, //methodname
            theArguments, argumentClass); //the arguments   
      returnValue=serializeResponse(result,tmr,holder,le,portTypeQName,null, isPayloadDocumentStyle);
    } catch (Exception e)
    {
        returnValue=serializeResponse(null,tmr,holder,le==null?new LiteralEnvelope(holder):le,portTypeQName,
        e, isPayloadDocumentStyle);
        returnValue[0]="error";
    } finally {
      /**
       * Log if payload received.  The logMessage
       * will log only if log is turned on.
       */
      logMessage(("Payload Sent: " + new String((byte[])returnValue[1])).getBytes());
      return returnValue;
    }
  } //execute
    
    /**
     * Method that scans the Headers in the envelope
     * to determine if mustUnderstand with value true
     * exists.  Returns true if it does exist, false
     * if not.
     * @param e the Envelope to interrogate.
     * @return boolean true if exists, false if not.

  private boolean mustUnderstandExists(LiteralEnvelope e) {
    Vector v = e.getHeader();
    if (v==null || v.size()==0)
      return false;
    for (int i=0; i<v.size(); i++) {
      HeaderEntry he = (HeaderEntry) v.elementAt(i);
      if (he.getMustUnderstand())
        return true;
    } //for
    return false;
  } //mustUnderstandExists
         */
    /**
     * Utility method to create a RouterException.
     * @param message the error message.
     * @param exceptionType the type of exception. This
     * is the SOAP Fault code.  The possible values
     * are defined in Fault.java
     */

  private RouterException createRouterException (String message,
						 String exceptionType) {
    RouterException re = new RouterException(message);
    if (exceptionType!=null && exceptionType.trim().length()>0)
      re.setExceptionType(exceptionType);
    return re;
  }//createRouterException
  
  /**
   * This method takes the SOAP payload which has a RPC style
   * body.  It parses the payload to determine the method
   * and the namespace.  The first element below the Body element 
   * without a root attribute or with root attribute!=0 is the
   * method name and its namespace corresponds to a Class.
   *
   * @param payload the SOAP payload as a byte stream.
   *
   * @return String[] with three elements.  String[0] contains
   * the method name and String[1] contains namespace. Returns
   * a null if the method or namespace cannot be determined.
   * String[2] For RPC style contains the name of the first parameter;
   * null if echoVoid operation.  For Document style contains null.
   *
   * @throws IOException if any error occurs while parsing the
   * payload
   *
   */
     
  private String[] initialParse(byte[] payload) throws RouterException {
    String[] returnValue = null;

    try {
      XmlParser parser = new XmlParser(new InputStreamReader
				       (new ByteArrayInputStream (payload)));
      /**
       * Keep reading and skipping till you get
       * to the Body element
       */
      while (true) {
        if (parser.peek().getType() == Xml.END_DOCUMENT) {
          break;  //should never happen
        }

        else if (parser.peek().getType() == Xml.START_TAG &&
           (parser.peek().getName().trim().equals("Fault") ||
            parser.peek().getName().trim().equals("Body")) )
        {
          break;
        }
        else
          parser.read();
      } //while true
      boolean rootFound=false;
      ParseEvent pe = parser.read(); //Get past the Body
      String endName=null;
      String endNamespace=null;
      while (true) 
      {
        pe = parser.read();
        if (pe.getType()==Xml.END_DOCUMENT)
          break;
        else if (pe.getType()==Xml.END_TAG &&
        pe.getName().equals(endName) && pe.getNamespace().equals(endNamespace))
        {
          break;
        }
        else if (pe.getType()==Xml.START_TAG) 
        {
           Attribute attrib=pe.getAttribute(Constants.SOAP_ENCODING_STYLE,"root");

          /**
           * If the root has already been found, we are interested in the
           * first parameter name.  This will help us get the correct Operation
           * from the WSDL.
           */
           if (rootFound)
           {
             returnValue[2]=pe.getName();
             break;
           }
           
          if (attrib==null || !(attrib.getValue().trim().equals("0"))) 
          {
            endName=pe.getName();
            endNamespace=pe.getNamespace();
            returnValue=new String[3];
            returnValue[2]=null; //put null in case this is a echoVoid andthere are no parameters.
            returnValue[0]=endName;
            returnValue[1]=endNamespace;
            rootFound=true;
            //break;
          } //if 
          else 
          {
            /**
             * Keep reading till you find the end tag
             * from the corresponding start tag.
             */
            while (true) 
            {
              if (parser.peek().getType()==Xml.END_TAG &&
              parser.peek().getName().equals(pe.getName())) 
              {
                break;
              }
              parser.read();
            } //while

          } //else
        } //else
      } //while
      return returnValue;
    } catch (IOException e) {
      throw this.createRouterException(e.getMessage(),
				       Fault.SERVER);
    }
	
  } /* initialParse */

    /**
     * This method takes an encapsulated Vector of 
     * <typemap>  and <elementmap> element from the 
     * deployment descriptor and constructs a TypeMappingRegistry 
     * object out of it.
     * @param Vector typemap - The <typemap> element in the deployment
     * descriptor encapsulated as a Vector.
     * @param Vector elementmap - The <elementmap> element in the 
     * deployment descriptor encapsulated as a Vector.
     * @return TypeMappingRegistry - returns a TypeMappingRegistry object
     */
  private TypeMappingRegistry getTypeMappingRegistry(Vector typemap,
						     Vector elementmap) 
    throws RouterException{
	
    TypeMappingRegistry tmr=null;
    try {
      if(typemap != null && typemap.size() > 0) {

        tmr = new TypeMappingRegistry();
        for(int j = 0; j < typemap.size(); j++) {
          String[] strArray = (String[]) typemap.elementAt(j);
          tmr.mapTypes(strArray[0],
                 strArray[1],
                 Class.forName(strArray[2]),
                 Class.forName(strArray[3]),
                 Class.forName(strArray[4]));
		
        } //for
      } //if
      if(elementmap != null && elementmap.size() > 0) {

	if (tmr==null) tmr = new TypeMappingRegistry();

	for(int j = 0; j < elementmap.size(); j++) {
	  String[] strArray = (String[]) elementmap.elementAt(j);
                     
	  /**
	   * Check to see if this is an array
	   * being mapped.
	   */
	  Class theClassName=null;
	  if (strArray[2]!=null &&
	      strArray[2]!="" &&
	      strArray[2].trim().equalsIgnoreCase("true")) {
	    theClassName=(Array.newInstance(Class.forName(strArray[1]),
					    1)).getClass();
	  }
	  else {
	    theClassName=Class.forName(strArray[1]);
	  }
	  if (strArray[3] !=null)
	    tmr.mapElements(strArray[0],
			    theClassName,
			    Class.forName(strArray[3]));
	  else
	    tmr.mapElements(strArray[0],
			    theClassName,
			    null);

	} //for
      } //if
      return tmr;
    } catch (Exception e) {
      throw this.createRouterException(e.getMessage(),
				       Fault.SERVER);
    }
	
  } //getTypeMappingRegistry
    
    /**
     * This method dynamically instantiates a service and executes
     * the method specified in the SOAP payload.
     * @param serviceClass the class name 
     * @param methodName the method name
     * @param argList the argument list.
     * @param argumentClass The class of the objects 
     * in argList.
     * @return Object[] index 0 contains the return
     * type and index 1 contains the return object.
     * @throws RouterException if any error occurs.
     */
  private Object[] invokeService(String serviceClass,
                                 String methodName, 
				 Object[] argList,
				 Class[] argumentClass) throws RouterException {
	
    try {
      // Get a Class for the class.

      Class theClass = Class.forName(serviceClass);
      Object[] returnValue=null;
      /**
       * Check to see if the method exists.  If not,
       * catch exception after the try block. If it
       * exists, determine the return type.  We are
       * only interested in void.
       * Commented out to since we do not need this
       * anymore.  The return datatype is now determined
       * from the WSDL
       * 
       */ 
      Method theMethod=theClass.getMethod(methodName,	 argumentClass);
      returnValue = new Object[2];
      
      /**
       * Added getName() to this line
       * If not, returnValue will include (i.e.) 
       * "class com.wingfoot.soap.Envelope"
       */
      returnValue[0]=theMethod.getReturnType().getName()+"";


      /**
       * Now that the method exists, create an instance
       * of the class.  
       */
      Object instance=theClass.newInstance();

      /**
       * Invoke the method.
       */

       /**
        * Check to see here for Java's bugs.
        * Our SOAP engine always returns arrays
        * as an array of Wrapper classes.
        * If the argumentClass is a primitive array then
        * JDK does not convert the argList into an array
        * of primitives.  This has to be done manually here.
        * argList and argumentClass
        */
        if (argumentClass!=null && argumentClass.length>0)
        {
          for (int i=0; i<argumentClass.length; i++)
          {
            if (argumentClass[i].isArray() &&
            argumentClass[i].getComponentType().isPrimitive())
            {
              argList[i] = convertWrappertoPrimitive(((Object[])argList[i]), 
              argumentClass[i].getComponentType());
            }//if primitive
          }//for
        }//if
        
      returnValue[1]= theMethod.invoke(instance,argList); 
      return returnValue;
    } catch (NoSuchMethodException e) {
      throw this.createRouterException("Cannot find method " +
				       methodName + 
				       " in class " +
				       serviceClass,
				       Fault.SERVER);
    }
    catch (InvocationTargetException e)
    {
            throw this.createRouterException(e.getTargetException().getMessage(),
				       Fault.SERVER);
    }
    catch (Exception e) {
      throw this.createRouterException(e.getMessage(),
				       Fault.SERVER);
    }
  } /* invokeService */

  /**
   * Takes a Wrapper array and converts it into a 
   * primitive array.  This is required becuase
   * Parvus SOAP engine returns only wrapper arrays and
   * JDK cannot automatically convert a wrapped array
   * into the corresponding primitive array.
   * @param wrapperArray the array encapsulated as its
   * wrapper
   * @param primitiveClass the class that the wrapperArray
   * has to be converted to.
   * @return Object the wrapperArray converted into its
   * primitive type.
   */
  private Object convertWrappertoPrimitive(Object[] wrapperArray, Class primitiveClass)
  {
    if (wrapperArray==null)
      return null;
    Object anArray = Array.newInstance(primitiveClass, wrapperArray.length);
    for (int i=0; i<wrapperArray.length; i++)
    {
      if (primitiveClass.getName().equals("boolean"))
        Array.setBoolean(anArray,i,((Boolean)wrapperArray[i]).booleanValue());
      else if (primitiveClass.getName().equals("byte"))
        Array.setByte(anArray,i,((Byte)wrapperArray[i]).byteValue()); 
      else if (primitiveClass.getName().equals("char"))
        Array.setChar(anArray,i,((Character)wrapperArray[i]).charValue());
      else if (primitiveClass.getName().equals("short"))
        Array.setShort(anArray,i,((Short)wrapperArray[i]).shortValue());
      else if (primitiveClass.getName().equals("int"))
        Array.setInt(anArray,i,((Integer)wrapperArray[i]).intValue());
      else if (primitiveClass.getName().equals("long"))
        Array.setLong(anArray,i,((Long)wrapperArray[i]).longValue());
      else if (primitiveClass.getName().equals("float"))
        Array.setFloat(anArray,i,((Float)wrapperArray[i]).floatValue());
      else if (primitiveClass.getName().equals("double"))
        Array.setDouble(anArray,i,((Double)wrapperArray[i]).doubleValue());
    }//for
    return anArray;
  }//convertWrapperToPrimitive

    
    /**
     * Write the payload to a file.  The file
     * is the value of the logger property. Prefixes
     * the log with a timestamp.
     */
  private void logMessage(byte[] payload) throws IOException {
    try {
      String fileName=getProperty("logger");
      if (fileName==null || payload==null)
        return;
      BufferedOutputStream bos = 
        new BufferedOutputStream(new FileOutputStream(fileName,true));
      byte[] currentTimestamp= new Date().toString().getBytes();
      bos.write('\n');
      bos.write(currentTimestamp,0,currentTimestamp.length);
      bos.write('\n');
      bos.write(payload,0,payload.length);
      bos.write('\n');
      bos.close();
    } catch (Exception e) {
      throw new IOException("Error writing to log file: " + e.getMessage());
    }
  } //logMessage

  /**
   * Determines whether a service is encoded or literal.  Given the methodName and wsdlholder,
   * and which messageType(InputMessage or OutputMessage), return true if the use is literal
   * else return false
   * @param holder The WSDLHolder 
   * @param methodName the method to lookup
   * @param messageType INPUT_MESSAGE or OUTPUT_MESSAGE
   * @param portTypeQName with a QName
   * @param boolean true if use is literal; else false
   */
  private boolean isUseLiteral (WSDLHolder holder, String methodName, 
  int messageType, QName portTypeQName)
  {
    PortType pType = holder.getPortType(portTypeQName);
    Binding binding=holder.getBinding(pType);
    for (int i=0; i<binding.getBindingOperationCount();i++)
    {
      BindingOperation bo = binding.getBindingOperation(i);
      if(bo.getOperation().getName().equals(methodName))
      {
        MessageFormat[] omf = null;
        if(messageType==INPUT_MESSAGE)
          omf = bo.getInputMessageFormat();
        else
          omf = bo.getOutputMessageFormat();
        if (omf!=null)
        {
          for (int j=0; j<omf.length; j++)
          {
            MessageFormat mf = omf[j];
            if(mf != null) 
            {
              if (mf instanceof SOAPMessage &&
              ((SOAPMessage)mf).getMessageType()==SOAPMessage.BODY &&
              ((SOAPMessage)mf).getUse()==SOAPMessage.LITERAL)
                return true;
              else
                return false;
            }

          }
        }//if
      } //if bo.operationName.equals(methodName)

    }//for
    return false;
  }//isUseLiteral
  
    /**
   * Gets the WSDLHolder in the hashtable via the WSDLLocation 
   * passed in.  If the WSDLHolder does not exist in the Hashtable,
   * fetch it using and convert it into a WSDLHolder, which is then returned
   * @param wsdlLocation The location of the WSDL.  This field is from the Service
   * object
   * @return WSDLHolder the WSDLHolder retrieved from the hashtable
   */
  private WSDLHolder getWSDLHolder(String WSDLLocation)
  throws IOException, Exception
  {
    if(wsdlHashtable.get(WSDLLocation) != null) {
      return (WSDLHolder) wsdlHashtable.get(WSDLLocation);
    }
    else
    {
      //retrieve the WSDLHolder, store it in the hashtable and return the 
      //correct WSDLHolder
      byte[] payload = XMLFactory.getPayload(WSDLLocation);
      WSDLHolder holder = (WSDLHolder) XMLFactory.parse(payload, WSDLLocation);
      if(holder != null)
      {
        synchronized (wsdlHashtable)
        {
          wsdlHashtable.put(WSDLLocation, holder);
          
        } //synchronize
      }
      return holder;
    } //else
  } //getWSDLHolder

    /**
   * Determines whether the uri contains a class name at the end
   * If the uri ends with "wserver", then the service is RPC, else 
   * document style
   * @param uri The uri to inspect
   * @return boolean True if document style, false otherwise
   */
  private boolean isPayloadDocumentStyle(String uri)
  {
    boolean isDocumentStyle = false;

    if(!uri.endsWith("/wserver")&& !uri.endsWith("/wserver/") && !uri.endsWith("/wserver/*"))
      isDocumentStyle = true;
      
    return isDocumentStyle;
  }

  /**
   * Retrieves from the Service, the method to invoke.
   * This is done using the parameterName 
   * parsed from the payload.  This method is used only for document style
   * services
   * @param parameterName the parameter name used for searching through the methods
   * stored in the service object
   * @param service The service object containing information about a particular service
   * @return String the method to invoke
   */
  private String getMethodFromServiceObject(String parameterName, Service service)
  {
    String methodToInvoke = null;
    Vector method = service.getMethod();
    if(method != null)
    {
      for(int i = 0; i <method.size(); i ++)
      {
        /**
         * inner vector contains method information.  
         * The first element is the methodName and the ones
         * following it are parameter names. An innerVector
         * of size 1 indicates a method that takes no parameter
         */
        Vector innerVector = (Vector) method.elementAt(i);
        if (innerVector!=null)
        {
          if(parameterName == null && (innerVector.size()==1 || innerVector.elementAt(1) == null))
          {
            methodToInvoke = (String) innerVector.elementAt(0);
            break;
          }
          else if(innerVector.size()>1 && innerVector.elementAt(1) !=null &&
          ((String[])innerVector.elementAt(1))[0].equals(parameterName))
          {
            methodToInvoke = (String) innerVector.elementAt(0);
            break;
          } 
        } 
        if(methodToInvoke != null)
          break;
      } //for
    } //method != null
    return methodToInvoke;
  }

  /**
   * Serializes resuls from the dynamically invoked class into a literal response
   * The return is an object array with the first element containing a string "ok"
   * and the second element containing the serialized payload for return to the client
   * @param results The results from the invoked class.  The first element is the type 
   * of the results and the second element is the return data itself
   * @param tmr TypeMappingRegistry used for serialization
   * @param holder WSDLHolder which contains information about this particular method/operation
   * @param envelope The received envelope, which contains schema and schemaInstance information
   * @param e Exception that encapsulates the fault.
   * @param isPayloadDocumentStyle boolean that identifies if the payload received by the SOAP server
   * was encoded or literal.  This is useful in cases where a Fault is thrown because the service
   * is not avialable.  In such a case it would be impossible to determine the style of the
   * return based on the WSDL (because there is no access to the WSDL).  In such a case
   * the style is identical to in input style.
   * @throws SOAPException A SOAPException is thrown if an error occurs during serialization
   */
  private Object[] serializeResponse(Object[] results, TypeMappingRegistry tmr, 
  WSDLHolder holder, LiteralEnvelope envelope, QName portTypeQName, Exception e, boolean isPayloadDocumentStyle)
  throws SOAPException
  {
    Object[] returnValue=new Object[2];
    
    LiteralEnvelope le = new LiteralEnvelope(holder);
    le.setSchema(envelope.getSchema());
    le.setSchemaInstance(envelope.getSchemaInstance());
    //le.setPortType(((LiteralEnvelope)envelope).getPortType());
    //le.setOperation(((LiteralEnvelope)envelope).getOperation());
    //le.setMessage(((LiteralEnvelope)envelope).getOperation().getOutputMessage());
    //if (results!=null)
      //le.setParameter(results[1]);

    if (e!=null) 
    {
        Fault f = new Fault();
        if (e instanceof RouterException) 
          f.setFaultCode(((RouterException) e).getExceptionType());
        else
          f.setFaultCode(Fault.SERVER);
        f.setFaultString(e.getMessage());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        e.printStackTrace(ps);
        f.setFaultDetail(new String(os.toByteArray()));
        le.setFault(f);
    }
    //else
    //{
    if (envelope!=null)
    {
      le.setPortType(((LiteralEnvelope)envelope).getPortType());
      le.setOperation(((LiteralEnvelope)envelope).getOperation());
      if (((LiteralEnvelope)envelope).getOperation()!=null)
        le.setMessage(((LiteralEnvelope)envelope).getOperation().getOutputMessage()); 
      if (results!=null)
        le.setParameter(results[1]);
    }
    //}
    Hashtable hashtable = new Hashtable();
    hashtable.put (Constants.SOAP_NAMESPACE,"SOAP-ENV");
    hashtable.put (le.getSchemaInstance(), "xsi");
    hashtable.put (le.getSchema(), "xsd");
    XMLWriter writer = new XMLWriter(hashtable);
    SOAPSerializer ss = null;

    if (le.getFault()!=null && envelope.getOperation()!=null)
    {
      if (this.isUseLiteral(holder,envelope.getOperation().getName(),this.OUTPUT_MESSAGE,portTypeQName))
        ss=new LiteralSerializer(le, tmr,writer);
      else
        ss=new SectionVSerializer(le,tmr,writer);
    }
    else if (isPayloadDocumentStyle)
    {
      ss=new LiteralSerializer(le, tmr, writer);
    }
    else
      ss=new SectionVSerializer(le, tmr, writer);
    
    returnValue[1] = ss.serialize();
    returnValue[0] = "ok";
    
    return returnValue;
  } //serializeLiteralResponse

  /**
   * Returns the PortType given a methodName
   */
  private PortType getPortType(WSDLHolder holder, String methodName)
  {
    PortType pType = null;
    PortType[] pt = holder.getPortType();
    if(pt != null)
    {
      for(int i = 0; i < pt.length; i++)
      {
        if(pt[i].getName().getLocalPart().equals(methodName.trim()))
          pType = pt[i];
      } //for
    } //if
    return pType;
  }
  
} /* com.wingfoot.soap.server.SOAPRouter */
