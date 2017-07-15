/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */

package com.wingfoot.soap;

import java.util.*;
import org.kxml.parser.*;
import org.kxml.*;
import java.io.*;
/**
 * Encapsulates a  Fault element in a SOAP message.
 * @since 0.90
 * @author Kal Iyer
 */
public class Fault {
    
  /**
   * private variables to store the contents
   * of the Fault
   */
  private String faultcode,faultstring;
  private Vector detail;
  public static String VERSIONMISMATCH = "VersionMismatch";
  public static String MUSTUNDERSTAND = "MustUnderstand";
  public static String CLIENT = "Client";
  public static String SERVER = "Server";
    
  /**
   * The default constructor.  Use this constructor
   * to create an instance of Fault
   * @since 0.90
   */
  public Fault() {}
    
  /**
   * Creates a new instance of Fault. 
   * This is the only constructor.
   * The  constructor parses the 
   * the Soap Fault.
   * @since 0.90
   * @param parser instance of XmlParser to
   * parse the Fault
   */
  public Fault(XmlParser parser) 
    throws IOException {
    this.parse(parser);
  } /* Fault(XmlParser) */

    /**
     * Parses the Fault in a SOAP message.
     * @since 0.90
     * @param parser instance of XmlParser
     * to parse the SOAP Fault element.
     */
  private void parse(XmlParser parser) throws IOException 
  {

    ParseEvent event;
    String name=null;
    while (true) 
    {
      //event = parser.read();
      event=parser.peek();
      if (event.getType()==Xml.END_TAG &&
      event.getName().trim().equals("Fault")) 
      {
        parser.read();
        break;
      }
      else if (event.getType() == Xml.START_TAG) 
      {
        name = event.getName().trim();
        if (name.equals("detail")) 
        {
          try 
          {
            detail=new Vector();
            parser.readTree(detail);
          } catch (Exception e){}
        } //if
        else 
          parser.read();
      } //else if
      else if (event.getType() == Xml.TEXT) 
      {
        event=parser.read();
        String text=event.getText();
        if (name.equals("faultcode"))
          faultcode=event.getText();
        else if (name.equals("faultstring"))
          faultstring=event.getText();
      } // else if
      else
        parser.read();
    } //while
  } /* parse */
     
  /**
   * Retrieves the <faultcode> element in
   * the Fault element of a SOAP message.
   * @since 0.90
   * @return String with the faultcode.
   * null if not faultcode is detected.
   */
  public String getFaultCode() {
    return faultcode;
  } /* getFaultCode() */
     
  /**
   * Retrieves the <faultstring> element
   * in the Fault element of a SOAP message.
   * @since 0.90
   * @return String with the faultstring,
   * null if not faultstring is detected.
   */
  public String getFaultString() {
    return faultstring;
  } /* getFaultString() */
     
  /**
   * Retrieves the <detail> element in
   * the Fault element of a SOAP message.
   * @since 0.90
   * @return Vector with each element of
   * the vector an instance of org.kxml.parser.ParseEvent
   */

  public Vector getDetail() {
    return detail;
  } /* getDetail */
     
  /**
   * Sets the fault code.
   * @since 0.90
   * @param faultcode the fault code
   */
  public void setFaultCode(String faultcode) {
    this.faultcode=faultcode;
  }
     
  /**
   * Sets the fault string.
   * @since 0.90
   * @param faultstring the fault string
   */
  public void setFaultString(String faultstring) {
    this.faultstring=faultstring;
  }
     
  /**
   * Sets the fault detail.
   * @since 0.90
   * @param faultdetail the fault detail
   */
  public void setFaultDetail(String detail) {
    if (this.detail==null) this.detail=new Vector();
    this.detail.addElement(detail);
  }

} /* class Fault */
