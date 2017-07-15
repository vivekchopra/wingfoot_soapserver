/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */

package com.wingfoot.soap.server;
import java.util.*;
import java.io.*;
/**
 * Java representation of a service in a  deployment descriptor.
 * The class is not public as it will be used within the server package.
 */

class Service implements Serializable {
     
  /**
   * The name attribute in the service element
   */
  private String serviceName;

  /**
   * The namespace element
   */
  private String serviceNamespace;

  /**
   * The classname element
   */
  private String serviceClass;

  /**
   * Encapsulates the method element(s). Each
   * element of the Vector is a Vector that
   * encapsulates a method name as the first
   * element and parameterNames as subsequent
   * elements.  
   * The inner vector starting from element 1 contains
   * a String[].  Element 0 contains the parameter name
   * and element 1 contains the type.
   */
  private Vector method;

  /**
   * Encapsulates the typemap element(s). Each
   * element of the Vector is a String[] that
   * encapsulates a typemap element.  The content
   * of the String[] is as follows:
   * String[0] - the typeNamespacea element
   * String[1] - the qname element
   * String[2] - the type element
   * String[3] - the Java2XMLClass element
   * String[4] - the XML2JavaClass element
   */
  private Vector typemap;

  /**
   * Determines whether the service is of RPC or document style
   */
  private boolean isDocumentStyle = false;
  
  /**
   * Encapsulates the elementmap element(s). Each
   * element of the Vector is a String[] that
   * encapsulates a elementmap element.  The content
   * of the String[] is as follows:
   * String[0] - the elementName element
   * String[1] - the Java class that is being mapped 
   * String[2] - contains true if the element is an array
   * null if not.
   * String[3] - XML2JavaClass; this is optional
   */
  private Vector elementmap;

  /**
   * Location of the WSDL for this web service
   */
  private String WSDLName;
  
  private String portTypeName;
  private String portTypeNamespace;

  String getServiceName() {
    return serviceName;
  }

  void setServiceName(String serviceName) {
    this.serviceName=serviceName;
  }

  String getServiceNamespace() {
    return serviceNamespace;
  }

  void setServiceNamespace(String serviceNamespace) {
    this.serviceNamespace=serviceNamespace;
  }
     
  String getServiceClass() {
    return serviceClass;
  }

  void setServiceClass(String serviceClass) {
    this.serviceClass=serviceClass;
  }

  void setIsDocumentStyle(boolean isDocumentStyle)
  {
    this.isDocumentStyle = isDocumentStyle;
  }
  
  Vector getMethod() {
    return this.method;
  }

  void setMethod(Vector method) {
    if(this.method == null)
      this.method = new Vector();
    this.method.add(method);
  }

  /*
  void setMethod(String method) {
    if (this.method==null)
      this.method=new Vector();
         
    this.method.add(method);
  }
  */

  Vector getTypemap() {
    return this.typemap;
  }

  void setTypemap(Vector typemap) {
    this.typemap=typemap;
  }

  void setTypemap(String[] typemap) {
    if (this.typemap==null)
      this.typemap=new Vector();
         
    this.typemap.add(typemap);
  }

  Vector getElementmap() {
    return elementmap;
  }

  void setElementmap(Vector elementmap) {
    this.elementmap=elementmap;
  }

  void setElementmap(String[] elementmap) {
    if (this.elementmap==null)
      this.elementmap=new Vector();
    this.elementmap.add(elementmap);
  }

  boolean isMethodExposed(String methodName) {
    boolean returnValue=false;
    if (methodName != null) {
      for (int i=0; i<method.size(); i++) {
        Vector innerVector = (Vector) method.elementAt(i);
        for(int j = 0; innerVector!=null && j < innerVector.size(); j++)
        {
          if (((String)innerVector.elementAt(0)).equals(methodName)) 
          {
            //returnValue=true;
            return true;
            //break;
          } //if
        }
      } //for
    } //if 
    //return returnValue;
    return false;
  } /*isMethodExposed*/

  void setWSDLName(String WSDLName)
  {
    this.WSDLName = WSDLName;
  }

  boolean isDocumentStyle()
  {
    return isDocumentStyle;
  }

  String getWSDLName()
  {
    return this.WSDLName;
  }

  public String getPortTypeName()
  {
    return portTypeName;
  }

  public void setPortTypeName(String newPortTypeName)
  {
    portTypeName = newPortTypeName;
  }

  public String getPortTypeNamespace()
  {
    return portTypeNamespace;
  }

  public void setPortTypeNamespace(String newPortTypeNamespace)
  {
    portTypeNamespace = newPortTypeNamespace;
  }

  public Vector getMethod(String methodName, String firstParameterName)
  {
    if(this.method == null)
      return null;
    for(int i = 0; i < this.method.size(); i++)
    {
      Vector innerVector = (Vector) this.method.elementAt(i);
      for(int j = 0; innerVector!=null&&j<innerVector.size(); j++)
      {
        if(((String)innerVector.elementAt(0)).equals(methodName))
        {
          String[] tmp = (String[]) innerVector.elementAt(1);
          if(tmp != null && tmp[0] != null && tmp[0].equals(firstParameterName))
            return innerVector;
        }
      } //inner for
    }
    return null;
  }

} /* com.wingfoot.soap.server.Service */
