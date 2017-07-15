package com.wingfoot.soap.encoding;
import com.wingfoot.soap.*;
import com.wingfoot.wsdl.*;
import com.wingfoot.xml.schema.*;
import com.wingfoot.xml.schema.types.*;
import org.kxml.parser.*;

/**
 * Defines properties that are required to convert a
 * parameter to a Java object based on the rules 
 * defined in XML schema.  Custom type deserializers
 * implement this interface.
 */
public interface TypeDeserializer 
{
  /**
   * Converts parameters in the parser to Java objects
   * based on the definition in the Component.
   * @param schemaComponent the Type that contains
   * the definition of the parameter 
   * @param javaBean Object where the properties of the
   * complexType has to be written to.
   * @param parser XmlParser that contains the payload. 
   * The parser is pointing to the start tag of the parameter.
   * @param deserializer SOAPDeserializer that is the entry
   * point to deserializing a parameter.
   * @param wsdlHolder WSDLHolder that encapsulates the WSDL.
   * @throws SOAPException if any error occurs during deserialization
   */
  public void unmarshall(Type schemaComponent, Object javaBean,
  XmlParser parser,
  SOAPDeserializer deserializer, WSDLHolder wsdlHolder) throws SOAPException;
}