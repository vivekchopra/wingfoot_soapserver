package com.wingfoot.soap.encoding;
import com.wingfoot.*;
import com.wingfoot.soap.*;
import com.wingfoot.wsdl.*;
import com.wingfoot.xml.schema.*;
import com.wingfoot.xml.schema.types.*;
import com.wingfoot.xml.schema.groups.*;
import org.kxml.*;
import org.kxml.parser.*;
import java.io.*;
import java.util.*;
/**
 * Takes a SOAP payload that uses literal encoding
 * and converts the parameters to Java parameters.
 */
public class LiteralDeserializer extends AbstractSerializer implements SOAPDeserializer
{
  private LiteralEnvelope envelope;
  private XmlParser parser;
  private TypeMappingRegistry tmr;
  private WSDLHolder wsdlHolder;
  private Hashtable deserializerCache;
  /**
   * Creates a LiteralDeserializer.
   * @param envelope the LiteralEnvelope where the 
   * parameters (deserialized from the SOAP payload)
   * is stored.
   * @param tmr TypeMappingRegistry that contains the
   * mapping between ComplexType and its corresponding
   * Java object.
   * @param parser the XmlParser that encapsulates the 
   * SOAP payload.
   * @throws SOAPException if the envelope, parser or
   * the WSDLHolder in the envelope is null.
   */
  public LiteralDeserializer(LiteralEnvelope envelope, TypeMappingRegistry tmr, XmlParser parser)
  throws SOAPException, IOException
  {
    super(envelope.getWSDLHolder(), parser);
    if (envelope==null || envelope.getWSDLHolder()==null ||
    parser==null)
      throw new SOAPException("ERROR_SOAPSERIALIZER_009:" + Constants.ERROR_SOAPSERIALIZER_009);
    this.wsdlHolder=envelope.getWSDLHolder();
    this.parser=parser;
    this.tmr=tmr;
    this.envelope=envelope;
  }//constructor.

  /**
   * Reads the SOAP payload and converts the parameters
   * that is encapsulated in the payload to Java parameters.
   * This is the entry point to the deserialization process.
   * At this point the parser is pointing to the Envelope
   * element.
   * <p>
   * The converted parameters are stored in the Envelope
   * passed in the constructor.
   * @throws IOException if an error occurs during deserialization.
   */
  public void deserialize() throws IOException, WSDLException,SOAPException
  {
    //Read till the <Body> element.
    skipTillBody();
    //Now skip to the next start tag.
    skipTillStartTag(this.parser);

    //Check to see the Fault.
    if (parser.peek().getType()==Xml.START_TAG &&
    parser.peek().getName().equals("Fault") &&
    parser.peek().getNamespace().equals(Constants.SOAP_NAMESPACE))
    {

      Fault f = new Fault(parser);
      envelope.setFault(f);
      return;
    }

    Binding binding=envelope.getWSDLHolder().getBinding(envelope.getPortType());
    if (binding!=null && binding.isOperationRPC(envelope.getOperation()))
    {
      skipOperationName();
    }
    /**
     * 
     * Convert the parameters in SOAP payload to Java objects
     * based on the Part in Message.
     */
     Message m = envelope.getMessage();
     if (m!=null && m.getMessagePart()!=null && m.getMessagePart().length>0) 
     {
       for (int i=0; i<m.getMessagePartCount(); i++) 
       {
         Part p=m.getMessagePart(i);
         envelope.setParameter(this.deserialize(p,parser));
       }
     }
  }//deserialize

  /**
   * Deserializes a message part.
   * @param Part the message part.
   * @param parser the XmlParser that is pointing to 
   * the message part.
   * @return Object the type encapsulated as Java object.
   * @throws IOException if an error occurs while using the parser
   * @throws SOAPException if the data type is unsupported.
   */
  public Object deserialize(Part part, XmlParser parser) throws IOException,SOAPException
  {
    if (part.getPartType()==Part.ELEMENT) 
    {
      Element e = wsdlHolder.getElement(part.getType().getTargetNamespace(),
      part.getType().getName());
      return deserialize(e, parser);
    }
    else 
    {
      Type type=super.getTypeFromReference(part.getType());
      return deserialize(type,parser,null);
    }
  }//deserialize(Part,XmlParser)

  /**
   * Takes a parameter encapsulated whose definition
   * is represented as an Element and converts it to
   * a Java representation.
   * @param element Element that contains the definition
   * of the data to convert to Java.
   * @param parser XmlParser that is pointing to the 
   * SOAP payload that contains the value for the element.
   * @return Object the type encapsulated as Java object.
   * @throws IOException if any error occurs while using
   * the parser.
   * @throws SOAPException if the data type is unsupported.
   */
  public Object deserialize(Element element, XmlParser parser) 
  throws IOException,SOAPException
  {
    if (element.getMaxOccurs() > 1)
      return deserializeElementArray(element,parser);
    else if (element.getType() instanceof TypeReference)
      return deserialize(super.getTypeFromReference((TypeReference)element.getType()),parser,element.getName());
    else
      return deserialize(element.getType(),parser,element.getName());
  }//deserialize(Element,XmlParser)

  /**
   * An array in XML Schema is represented as an element where
   * the maxoccurs is >1.  This method creates an array object.
   */
  private Object deserializeElementArray(Element element, XmlParser parser) 
  throws IOException, SOAPException
  {
    Vector returnVector=null;
    Vector returnTypes=null;
    String endName=element.getName();
    super.skipTillStartTag(parser);
    while (true) 
    {
      ParseEvent pe=parser.peek();
      if (pe.getType()==Xml.END_DOCUMENT ||
      (pe.getType()==Xml.END_TAG && (!pe.getName().equals(endName)))) 
      {
        //Bug fix for version 1.1.  The line below is commented out.
        //parser.read();
        break;
      }
      //Begin fix for version 1.1
      else if (pe.getType()==Xml.START_TAG && (!pe.getName().equals(endName)))
      {
        break;
      }
      //End fix for version 1.1
      else if (pe.getType()==Xml.START_TAG &&
      pe.getName().equals(endName)) 
      {
       //Store the return types.  This will be useful to convert
       //the Vector to array.
       try
       {
         Class clazz = super.getJavaType(element.getType(),tmr);
         if (returnTypes==null) returnTypes=new Vector();
         returnTypes.add(clazz);
       } catch (ClassNotFoundException e) 
       {
         throw new SOAPException(e.getMessage());
       }
       
       Object retValue=(this.deserialize(element.getType(),parser,element.getName()));
       if (returnVector==null) returnVector=new Vector();
       returnVector.add(retValue);
      }
      else 
      {
        parser.read();
      }
    }//while
    if (returnVector==null ||returnVector.size()==0)
      return null;
    else
    {
      try
      {
        return super.getArrayFromVector(returnVector, returnTypes);
      } catch (ClassNotFoundException e)
      {
        throw new SOAPException(e.getMessage());
      }
    }
  }//deserializeElementArray

  /**
   * Deserializes a Literal Type Array.  Literal arrays
   * are ComplexType with the array elements wrapped with
   * the complex type name.
   */
  private Object deserializeArray(ComplexType ct, XmlParser parser)
  throws SOAPException, IOException
  {
    boolean wrapperStartTagFound=false;
    while (true)
    {
      //Read till you get past the wrapper and
      //point the parser to the start tag after wrapper
      ParseEvent pe=parser.peek();
      if (pe.getType()==Xml.END_DOCUMENT)
        return null;
      //else if (pe.getType()==Xml.START_TAG &&
     // pe.getName().equals(ct.getName()) &&
     // pe.getNamespace().equals(ct.getTargetNamespace()))
       // break;
      else if (pe.getType()==Xml.START_TAG &&wrapperStartTagFound)
        break;
      else if (pe.getType()==Xml.START_TAG)
        wrapperStartTagFound=true;
      parser.read();
    }//while
    //Get the element from ComplexType.
    ComplexContent cc=(ComplexContent)ct.getContent();
    List l = cc.getContentList();
    ModelGroupImplementation mgi=((ModelGroupImplementation)l.get(0));
    List ll = mgi.getContent();
    Element e = (Element)ll.get(0);
    return deserialize(e,parser);
  }//deserializeArray
  
  /**
   * Takes a Type and converts it to a Java representation.
   * The Type is either a XSDType, SimpleType.
   * @param type Type that contains the definition of the
   * data type encapsulated in the parser.  It is either
   * a XSDType or SimpleType.
   * @param XmlParser that is pointing to the StartTag
   * of the element that contains the Type value.
   * @param defaultTypeName the name of the type.  This is
   * typically necessary for anonymous simple type and complex types.
   * For named types, this may be left null.  For anonymous types,
   * the deserializer uses the name to look up the type mapping registry
   * to determine the appropriate deserializer class.
   * @return Object the type encapsulated as Java object.
   * @throws SOAPException if the type is not supported.
   * @throws IOException if an error occurs while processing
   * the payload.
   */
   
  public Object deserialize(Type type, XmlParser parser, String defaultTypeName)
  throws IOException, SOAPException
  {
    //Get the TypeMappingRegistry here.
    try
    {
      super.skipTillStartTag(parser);
      ParseEvent pe=parser.peek();
      org.kxml.Attribute nilValue=parser.peek().getAttribute(Constants.SOAP_SCHEMA_INSTANCE,"nil");
      if (nilValue!=null && nilValue.getValue()!=null && (nilValue.getValue().equals("1")
      || nilValue.getValue().equals("true")))
      {
        parser.read();
        return null;
      }
      String[] strarray = tmr != null ? 
      tmr.getInfoForNamespace(type.getTargetNamespace(),
      type.getName()==null?defaultTypeName:type.getName()):null;   
      if (strarray != null) 
      {
        TypeDeserializer deserializer=this.getDeserializerCache(strarray[0]);
        if(deserializer==null)
        {
          Class serializerClass = Class.forName(strarray[2]);
          deserializer=(TypeDeserializer) serializerClass.newInstance();
          setDeserializerCache(strarray[0], deserializer);
        }
        Object beanInstance=Class.forName(strarray[0]).newInstance();
        deserializer.unmarshall(type,beanInstance,parser,this,wsdlHolder);
        return beanInstance;
      }
      else if (type instanceof ComplexType && ((ComplexType)type).isLiteralArray())
        return deserializeArray(((ComplexType)type),parser);
      else if (type instanceof XSDType)
        return deserializeXSDType((XSDType)type, parser);
      else if (type instanceof SimpleType)
        return deserializeSimpleType((SimpleType)type, parser); 
      else if (type instanceof TypeReference)
        return deserialize(super.getTypeFromReference((TypeReference)type),parser,defaultTypeName);
      else
        throw new SOAPException("ERROR_SOAPDESERIALIZER_002:"+
        Constants.ERROR_SOAPDESERIALIZER_002);
    } catch (Exception e)
    {
      throw new SOAPException(e.getMessage());
    }
  }//deserialize(String, Type, parser)

  /**
   * Takes a Type and a String value and returns back the Java representation.
   * Typically used to deserialize an attribute.
   * @param type the Type that contains the definition of the value.  Should
   * be either a XSDType or SimpleType.
   * @param value the value of the attribute.
   */
  public Object deserializeAttribute(Type type,String value) throws SOAPException, IOException
  {
    if (type instanceof XSDType)
      return deserializeXSDType((XSDType)type, value);
    else if (type instanceof SimpleType)
      return deserializeSimpleType((SimpleType)type, value); 
    else
      throw new SOAPException("ERROR_SOAPDESERIALIZER_002:"+Constants.ERROR_SOAPDESERIALIZER_002);
  }

  /**
   * Takes a data type inbuilt in XML schema and converts
   * it to a Java representation.  The supported types are
   * <li> xsd:string
   * <li> xsd:byte
   * <li> xsd:short
   * <li> xsd:int
   * <li> xsd:long
   * <li> xsd:float
   * <li> xsd:double
   * <li> xsd:boolean
   * <li> xsd:date
   * <li> xsd:decimal
   * <li> xsd:base64binary
   * <li> xsd:hexbinary
   * @param type the inbuilt data type encapsulated as a
   * XSDType
   * @param parser XmlParser that is pointing to the XSDType
   * @throws SOAPException if the type is unsupported
   * @throws IOException if an error occurs while using the 
   * parser.
   */
   private Object deserializeXSDType(XSDType type, XmlParser parser) throws
   IOException, SOAPException
   {
      String xsdLocalPart=type.getName().trim().toLowerCase();
      String typeValue=null;
      super.skipTillStartTag(parser);
      String endTagName=parser.peek().getName();
      while (true)
      {
        ParseEvent pe=parser.peek();
        if (pe.getType()==Xml.END_DOCUMENT ||
        (pe.getType()==Xml.END_TAG && pe.getName().equals(endTagName) ))
        {
          parser.read();
          break;
        }
        else if (pe.getType()==Xml.TEXT)
        {
          typeValue=pe.getText();
          parser.read();
        }
        else
          //Not sure what this is.  Just read.
          parser.read();
    }//while
    return deserializeXSDType(type,typeValue);
   }
   
  private Object deserializeXSDType(XSDType type, String typeValue)
  throws IOException, SOAPException
  {
    String xsdLocalPart=type.getName().trim().toLowerCase();
    if (typeValue==null)
      return null;
    else if (xsdLocalPart.equals("string"))
      return new String(typeValue);
    else if (xsdLocalPart.equals("byte"))
      return new Byte(Byte.parseByte(typeValue));
    else if (xsdLocalPart.equals("short"))
      return new Short(Short.parseShort(typeValue));
    else if (xsdLocalPart.equals("int")||xsdLocalPart.equals("integer"))
      return new Integer(Integer.parseInt(typeValue));
    else if (xsdLocalPart.equals("long"))
      return new Long(Long.parseLong(typeValue));
    else if (xsdLocalPart.equals("float"))
    {
      if (typeValue.equalsIgnoreCase("nan"))
        return new Float(Float.NaN);
      else if (typeValue.equalsIgnoreCase("inf") ||
	    typeValue.equalsIgnoreCase("infinity"))
			  return new Float(Float.POSITIVE_INFINITY);
      else if (typeValue.equalsIgnoreCase("-inf")||
	    typeValue.equalsIgnoreCase("-infinity"))
	      return new Float(Float.NEGATIVE_INFINITY);
      else
        return new Float(Float.parseFloat(typeValue));
    } //float
    else if (xsdLocalPart.equals("double"))
    {
      if (typeValue.equalsIgnoreCase("nan"))
        return new Double(Double.NaN);
      else if (typeValue.equalsIgnoreCase("inf") ||
	    typeValue.equalsIgnoreCase("infinity"))
			  return new Double(Double.POSITIVE_INFINITY);
      else if (typeValue.equalsIgnoreCase("-inf")||
	    typeValue.equalsIgnoreCase("-infinity"))
	      return new Double(Double.NEGATIVE_INFINITY);
      else
        return new Double(Double.parseDouble(typeValue));
    }
    else if (xsdLocalPart.equals("decimal"))
      return new java.math.BigDecimal(typeValue);
    else if (xsdLocalPart.equals("date") || xsdLocalPart.equals("datetime"))
      return super.deserializeDate(typeValue);
    else if (xsdLocalPart.equals("boolean"))
    {
       if (typeValue.equalsIgnoreCase("true") || typeValue.equals("1"))
        return new Boolean(true);
      else if (typeValue.equalsIgnoreCase("false") || typeValue.equals("0"))
        return new Boolean(false);
      else
        throw new SOAPException("Incorrect value detected for boolean");
    }//boolean
    else if (xsdLocalPart.equals("base64binary"))
      return new Base64(typeValue);
    else if (xsdLocalPart.equals("hexbinary"))
      return new HexBinary(typeValue);
    else if (xsdLocalPart.equals("ur-type") || xsdLocalPart.equals("anyType"))
      return (Object) (new String (typeValue));
    else
      throw new SOAPException("Unsupported data type:" +xsdLocalPart );
  }//serializeXSDType

  /**
   * Takes a SimpleType and returns back the 
   * Java representation.  Since a SimpleType
   * is nothing but a restriction on a XSDType
   * the actual work is done by deserializeXSDType.
   * @param type the SimpleType to deserialize
   * @param parser XmlParser that points to the
   * simpleType.
   * @throws SOAPException if the type is unsupported
   * @throws IOException if an error occurs while using the 
   * parser.
   */
  private Object deserializeSimpleType(SimpleType type, XmlParser parser) 
  throws SOAPException, IOException
  {
      String st = type.toString();
      String ns=st.substring(0,st.indexOf(':')+1);
      String name=st.substring(st.indexOf(':')+1);
      XSDType xsd=new XSDType(new QName(ns,name));
      return this.deserializeXSDType(xsd,parser);
  }//serializeSimpleType

  private Object deserializeSimpleType(SimpleType type, String value) 
  throws SOAPException, IOException
  {
      String st = type.toString();
      String ns=st.substring(0,st.indexOf(':')+1);
      String name=st.substring(st.indexOf(':')+1);
      XSDType xsd=new XSDType(new QName(ns,name));
      return this.deserializeXSDType(xsd,value);
  }
  
  /**
   * Reads the XmlParser until the <Body> element is
   * encountered.  The parser is positioned so that
   * the next parser.peek() or parser.read() returns
   * the element after the <Body> element.
   */
  protected void skipTillBody() throws IOException
  {
    while (true)
    {
      ParseEvent pe=parser.peek();
      if (pe.getType()==Xml.END_DOCUMENT)
        break;
      else if (pe.getType()==Xml.START_TAG &&
      pe.getName().equals("Body") && 
      pe.getNamespace().equals(Constants.SOAP_NAMESPACE))
      {
        parser.read();
        break;
      }
      else
        //do not know what this is.  Just read
        parser.read();
    }//while
  }//skipTillBody

  /**
   * Skips the wrapper element wrapped around 
   * the parameter in a SOAP body. When this
   * method is called, the parser is positioned
   * at the Body element.
   */
  private void skipOperationName() throws IOException
  {
    while (true)
    {
      ParseEvent pe=parser.peek();
      if (pe.getType()==Xml.END_DOCUMENT)
        break;
      else if (pe.getType()==Xml.START_TAG)
      {
        parser.read();
        break;
      }
      else
        parser.read();
    }//while
  }//skipOperationName 

  public void setDeserializerCache(String beanClassName, TypeDeserializer instance)
  {
    if (deserializerCache==null)
      deserializerCache=new Hashtable();
    deserializerCache.put(beanClassName,instance);
  }

  public TypeDeserializer getDeserializerCache(String className)
  {
    if (deserializerCache==null)
      return null;
    return (TypeDeserializer) deserializerCache.get(className);
  }
}//class