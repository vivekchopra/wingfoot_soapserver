package com.wingfoot.soap.encoding;
import org.kxml.io.*;
import com.wingfoot.xml.schema.types.*;
import com.wingfoot.wsdl.*;
import com.wingfoot.soap.*;
/**
 * Converts a Java Type to XML.  Implementations
 * of this interface take a Java object and
 * convert it to a SOAP format based on either
 * SOAP section V encoding rules or literal
 * encoding rules.
 * <p>
 * Custom serializers are created by implementing
 * this interface.  During runtime, such custom
 * interfaces are registered in TypeMappingRegistry.
 * During the serialization process, when the data
 * type with a custom serializer mapped to it is
 * encountered, the toolkit calls the custom serializer's
 * marshall method.
 */
public interface TypeSerializer 
{
  /**
   * Takes a Java object and its corresponding
   * XML Schema definition and converts it to
   * a SOAP payload. The SOAP payload is written
   * to the XMLWriter.
   * <p>
   * This method is called by the toolkit on
   * encountering a data type that is mapped
   * to an instance of this Interface.  The
   * user has no need to call this method.
   * @param parameterNS String with the namespace
   * of the element (that is written in XMLWriter)
   * that contains the type definition
   * of the Java object.
   * @param parameterName String with the name
   * of the element (that is written in XMLWriter)
   * that contains the type definition of the 
   * Java object.
   * @param writer the XMLWriter to write the SOAP
   * payload to.
   * @param type One of the possible Type defined
   * in com.wingfoot.xml.schema.types.  The Type
   * holds the XML definition of the objectToMarshall
   * @param objectToMarshall the Java object to convert
   * to XML.  This parameter may be null.
   * @param objectClass the Class of objectToMarshall.
   * This is required as the objectToMarshall may
   * be null; in such a case, the class may not be
   * determined from objectToMarshall.
   * @param registry TypeMappingRegistry that contains
   * the maps a Java class to its serializer.
   * @param as AbstractSerializer.  Implementations
   * of this interface may use this class to serialize
   * individual properties.
   * @param wsdlHolder WSDL encapsulated as a WSDLHolder.
   * The WSDLHolder contains the definition of the method
   * and the Java class being serialized.
   * @throws SOAPException if an error occurs during
   * serialization.
   */
  public void marshall(String parameterNS, String parameterName,
  XMLWriter writer, Type type,
  Object objectToMarshall, Class objectClass,
  TypeMappingRegistry registry, SOAPSerializer as,
  WSDLHolder wsdlHolder) throws SOAPException;


}//class TypeSerializer