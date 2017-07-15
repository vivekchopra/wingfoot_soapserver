package com.wingfoot.soap.encoding;
import com.wingfoot.soap.*;
import com.wingfoot.wsdl.*;
import com.wingfoot.xml.schema.*;
import com.wingfoot.xml.schema.types.*;
import org.kxml.parser.*;
import java.io.*;
/**
 * Defines the properties that are required for a 
 * class to deserialize a SOAP payload.
 */
public interface SOAPDeserializer 
{
  public boolean shouldElementBeNamespaceQualified(Element e);

  public boolean shouldAttributeBeNamespaceQualified(com.wingfoot.xml.schema.Attribute attribute);

  public void deserialize() throws IOException, WSDLException, SOAPException;
  
  public Object deserialize(Type type, XmlParser parser, String defaultTypeName) 
  throws IOException, SOAPException;

  public Type getTypeFromReference(TypeReference tr); 

  /**
   * Takes a Type and a String value and returns back the Java representation.
   * Typically used to deserialize an attribute.
   * @param type the Type that contains the definition of the value.  Should
   * be either a XSDType or SimpleType.
   * @param value the value of the attribute.
   */
  public Object deserializeAttribute(Type type,String value) throws SOAPException, IOException;

  public Object deserialize(Element element, XmlParser parser) 
  throws IOException,SOAPException;
}