/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */

package com.wingfoot.soap;

import java.util.*;
import org.kxml.*;
import org.kxml.io.*;
/**
 * Base class for all SOAP message elements. This
 * class is never instantiated.
 * @since 0.90
 * @author Kal Iyer
 */

public abstract class SOAPElement {

  private Hashtable attributes;  // Hashtable of SOAPElement attributes
    
  /**
   * Creates a new instance of SOAPElement.
   * @since 0.90.
   */
  public SOAPElement () {
  }

  /**
   * Adds an attribute
   * @since 0.90.
   * @param attributeName  Attribute name
   * @param attributeValue Attribute value
   */
  public void addAttribute (String attributeName,
			       String attributeValue) {

    if (attributes==null) attributes=new Hashtable();
    attributes.put (attributeName, attributeValue);
  }
    
  /**
   * Removes an attribute
   * @since 1.00.
   * @param attributeName the key to remove from the
   * Hashtable.
   */

  public void removeAttribute(String attributeName) {
    attributes.remove(attributeName);
  }

  /**
   * Returns a (previously set) SOAPElement attribute
   * @since 0.90.
   * @return the value of the attribute.
   */
  public String getAttribute (String attributeName) {
    return (attributes != null) ? 
      (String) attributes.get (attributeName) : null;
  }

  /**
   * Returns all the SOAPElement attributes
   * @since 0.90.
   * @return Hashtable of name/value pairs; null
   * if no attribute is set.
   */
  public Hashtable getAttributes () {
    return attributes;
  }

  /**
   * Converts the attributes in the Hashtable
   * to attributes of an element. Called by
   * subclasses when serializing iteself.
   * @since 0.90.
   * @param xmlwriter instance of XMLWriter to
   * aid in writing XML.
   */
  public void serialize(XMLWriter xmlwriter) {
    if (attributes==null) return;
    Enumeration keys = attributes.keys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      xmlwriter.attribute(key, getAttribute(key));
    }
  }
    
  /**
   * Encapsulates  attributes in XML form in a Hashtable
   * of name-value pairs.
   * @since 0.90.
   * @param theAttributes Vector, with each element an 
   * instance of org.kxml.Attribute.
   */
  public void deserialize(Vector theAttributes) {
    if (theAttributes ==null)
      return;
    for (int i=0; i<theAttributes.size(); i++) {
      Attribute attribute = (Attribute) theAttributes.elementAt(i);
      addAttribute(attribute.getName(),
		   attribute.getValue());
    } //for
  } /* deserialize(XmlParser, Vector) */

} /* com.wingfoot.soap.Element */
