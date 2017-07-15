/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */

package com.wingfoot.soap;

import java.util.*;
import org.kxml.io.*;
import com.wingfoot.*;
/**
 * Encapsulates an element below the
 * <SOAP-ENV:Header> element in a SOAP
 * message.  The presence of <SOAP-ENV:Header>
 * element is optional.
 * @since 0.90
 * @author Kal Iyer
 */

public class HeaderEntry extends SOAPElement {
  private String   headerName;  // Header entry name
  private String   headerValue; // Header entry value

  private boolean mustUnderstand;

  /**
   * Constructor for HeaderEntry.  
   * @since 0.90
   * @param headerName  the name of the
   * element below the <SOAP-ENV:Header>
   * element.
   * @param headerValue the text value
   * for the headerName.
   */
  public HeaderEntry (String headerName,
		      String headerValue) {
        
    // Initialize class data elements
    this.headerName     = headerName;
    this.headerValue    = headerValue;
  } /* constructor */

    /**
     * Convenience method to return the value
     * of the actor attribute.
     * @since 0.90
     * @return the value of the actor attribute; 
     * null if no actor is set.
     */
  public String getActor () {
    return super.getAttribute ("actor");
  } /* getActor */

    /**
     * Convenience method to return the header
     * name. 
     * @since 0.90
     * @return  the name of the Header; null if
     * no name is set.
     */
  public String getHeaderName () {
    return headerName;
  } /* getHeaderName */

    /**
     * Convenience method to return the namespace
     * associated with the header entry.
     * @since 0.90
     * @return the namespace associated with the
     * header entry; Constants.DEFAULT_NAMESPACE
     * if no namespace is specified.
     */
  public String getNamespace() {

    return (getAttribute("namespace")!=null) ?
      getAttribute("namespace") :
      Constants.DEFAULT_NAMESPACE;
  } /* getNamespace*/

    /**
     * Convenience method for returning the mustUnderstand attribute
     * @since 0.90
     * @return true if mustUnderstand is set to true, false if set
     * to false or not specified at all.
     */
  public boolean getMustUnderstand () {
    String str=null;
    str =  super.getAttribute ("SOAP-ENV:mustUnderstand");
    if (str==null)
      str =  super.getAttribute ("mustUnderstand");
    if (str == null)
      return false;
    else
      return str.equals ("1")?true:false;
  } /* getMustUnderstand*/

    /**
     * Returns the header value
     * @since 0.90
     * @return the header value
     */
  public String getValue () {
    return headerValue;
  } /* getValue */

    /**
     * Convenience method for setting the <i>actor</i> attribute.
     * @since 0.90
     * @param target Target endpoint. 
     * <br> A target with value http://schemas.xmlsoap.org/soap/actor/next 
     * indicates that the entry is targeted to the first endpoint that 
     * finds it. A missing actor attribute indicates that the entry is
     * targetted towards the final endpoint.
     */
  public void setActor (String target) {
    super.addAttribute ("actor", target);
  } /* setActor */

    /**
     * Convenience method for setting the <i>namespace<i>. This sets the 
     * namespace as xmlns:namespacePrefix="namespaceURI"
     * @since 0.90
     * @param namespacePrefix Namespace Prefix
     * @param namespaceURI    Namespace URI
     */
  public void setNamespace (String namespaceURI ) {
    super.addAttribute("namespace", namespaceURI);
  } /* setNamespace */

    /**
     * Convenience method for setting the mustUnderstand attribute
     * @since 0.90
     * @param mustUnderstand mustUnderstand flag
     */
  public void setMustUnderstand (boolean mustUnderstand) {
    if (mustUnderstand)
      super.addAttribute ("SOAP-ENV:mustUnderstand", "1");
  } /* setMustUnderstand */

    /** 
     * Converts SOAP Headers to XML.  This method is only
     * called by Envelope when it is serializing itself.
     * @since 0.90
     * @param xmlwriter instance of XMLWriter to aid in
     * writing the XML.
     */
  public void serialize (XMLWriter xmlwriter) {
    if (getNamespace() != null)
      xmlwriter.startElement (getHeaderName(),getNamespace());
    else
      xmlwriter.startElement (getHeaderName());
    super.serialize(xmlwriter);
    xmlwriter.elementBody (getValue());
    xmlwriter.endTag ();
  } /* serialize */

} /* com.wingfoot.soap.HeaderEntry */
