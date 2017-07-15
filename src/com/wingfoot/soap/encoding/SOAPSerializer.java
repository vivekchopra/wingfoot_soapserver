package com.wingfoot.soap.encoding;
import com.wingfoot.soap.*;
import com.wingfoot.xml.schema.*;
/**
 * Defines the properties that are required
 * for a class to serialize a SOAP payload.
 */
public interface SOAPSerializer 
{
  /**
   * Takes one parameter and its definition in the WSDL
   * and converts it to XML.
   * @param element the Element in WSDL that contains
   * the definition of the parameterValue.
   * @param parameterValue the value of the parameter.
   */
  public abstract void serializeParameter(Element element,Object parameterValue) 
  throws SOAPException;

  /**
   * Converts an Envelope to a SOAP payload.  Concrete
   * implementations of this interface decide the rules used
   * to convert the parameters to SOAP payload.  The 
   * concrete implementation also stores the Envelope.
   * @return byte the binary representation of the 
   * SOAP payload
   * @throws SOAPException if any error occurs while
   * generating the payload.
   */
  public byte[] serialize() throws SOAPException;
  
  public boolean shouldElementBeNamespaceQualified(Element e);

  public boolean shouldAttributeBeNamespaceQualified(Attribute attribute);
}