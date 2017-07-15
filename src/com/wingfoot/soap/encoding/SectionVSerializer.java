package com.wingfoot.soap.encoding;
import com.wingfoot.*;
import com.wingfoot.xml.schema.*;
import com.wingfoot.xml.schema.types.*;
import com.wingfoot.soap.*;
import com.wingfoot.wsdl.*;
import org.kxml.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * Converts an Envelope (a.k.a serializes an Envelope) to a SOAP payload based on
 * the rules specifed in <a href="http://www.w3.org/TR/SOAP/#_Toc478383512">SOAP 1.1 Section V</a>.
 * A SOAP payload created using Section V rules is popularly refered to as 
 * encoded using Section V encoding.
 */
public class SectionVSerializer extends LiteralSerializer implements SOAPSerializer
{
  WSDLHolder wsdlHolder;
  LiteralEnvelope envelope;
  TypeMappingRegistry tmr;
  XMLWriter writer;
  /**
   * Creates an instance of SectionVSerializer.  
   * @param wsdlHolder a WSDLHolder than contains the definition
   * of the operations and parameters that is used to serialize
   * the payload.
   * @param tmr TypeMappingRegistry that maps the ComplexType to
   * a Java class.
   */
  public SectionVSerializer(LiteralEnvelope envelope,TypeMappingRegistry tmr, XMLWriter writer)
  throws SOAPException
  {
    super(envelope,tmr,writer);
    this.wsdlHolder=envelope.getWSDLHolder();
    this.envelope=envelope;
    this.tmr=tmr;
    this.writer=writer;
  }//constructor


  /**
   * Takes each parameter in the Envelope and converts
   * into SOAP payload according to the rules specified
   * in SOAP 1.1 Section V.  It is able to handle
   * both RPC style and document style payload.
   * @return byte array of byte that is the binary
   * representation of the SOAP payload.
   * @throws SOAPException if any error occurs while
   * generating the payload.
   */
  public byte[] serialize() throws SOAPException
  {
    try 
    {
        writer.startElement ("Envelope", Constants.SOAP_NAMESPACE);
        /**
         * Put the encoding style here, if applicable.
         */
        if (envelope.getWSDLHolder()!=null)
        {
          String es=envelope.getEncodingStyle();
          if (es!=null)
            envelope.addAttribute("SOAP-ENV:encodingStyle", es);
        }
        envelope.serialize(writer);
        writeBody();
        writer.endTag(); //ends the Envelope.
        return writer.getPayload("UTF-8");
    } catch (Exception e) 
    {
      throw new SOAPException(e.getMessage());
    }
  }

    /**
   * Creates the <Body> element.
   */
  private void writeBody() throws SOAPException, WSDLException
  {
      boolean isRPC=false;
      //write the Body element
      writer.startElement ("Body", Constants.SOAP_NAMESPACE);
      // If necessary, write the SOAP Fault
      if (envelope.getFault() !=null)
      {
        writeSOAPFault(envelope.getFault());
        writer.endTag(); //close fault
      }
      else
      {
          Binding binding=wsdlHolder.getBinding(envelope.getPortType());
          if (binding.isOperationRPC(envelope.getOperation()))
          {
            String namespace=getNamespaceForOperation(binding,envelope.getOperation(),
            envelope.getMessage());
            writer.startElement(envelope.getOperation().getName(),namespace);
            isRPC=true;
          }
          Message m = envelope.getMessage();
          Vector v = envelope.getParameter();
          for (int i=0; i<m.getMessagePartCount();i++)
          {
              Part p = m.getMessagePart(i);
              if (p.getPartType()==Part.ELEMENT)
                throw new WSDLException("ERROR_SOAPSERIALIZER_010:"+Constants.ERROR_SOAPSERIALIZER_010);
              this.serializeParameter(p, v.elementAt(i), binding);
          }
          if (isRPC) 
            writer.endTag(); //close the body element.
          writer.endTag(); //end the Body element.
      }//if not fault
  }//writeBody

  /**
   * Converts a parameter whose data type is encapsulated
   * in the WSDL as a Type into XML stub.
   * @param type the definition of the parameterValue
   * @param parameterValue the value of the parameter to
   * be converted to XML.
   * @throws SOAPException if the type is null.
   */
  public void serializeParameter(Part part, Object parameterValue, Binding binding)
  throws SOAPException
  {
    Type partType=null;
    partType=part.getType();
    if (partType instanceof TypeReference)
      partType=getTypeFromReference((TypeReference)partType);
    if (partType==null)
      throw new SOAPException("ERROR_SOAPSERIALIZER_011:" + Constants.ERROR_SOAPSERIALIZER_011);

    if (partType instanceof ComplexType && ((ComplexType)partType).isArray())
    {
      serializeArray(part.getPartName(),(ComplexType)partType,parameterValue,binding);
    }
    /*
    else if (partType instanceof ComplexType && ((ComplexType)partType).isHashMap())
    {
    }
    else if (partType instanceof ComplexType && ((ComplexType)partType).isHashMap()) 
    {
    }
    */
    else
    {
      //Just call the serializer in LiteralSerializer
      super.serializeParameter(part,parameterValue);
    }
  }//serializeParameter
  
  private void writeSOAPFault(Fault faultObject) throws SOAPException
  {
    writer.startElement("Fault",Constants.SOAP_NAMESPACE);
    writer.startElement("faultcode");
    writer.elementBody("SOAP-ENV:"+faultObject.getFaultCode());
    writer.endTag();

    writer.startElement("faultstring");
    writer.elementBody(faultObject.getFaultString());
    writer.endTag();
        
    writer.startElement("detail");

    if (faultObject.getDetail() != null) 
    {
      Vector v = faultObject.getDetail();
      for (int i=0; i<v.size(); i++) 
        writer.elementBody(v.elementAt(i)+"");
        v=null;
    } 
    writer.endTag();
    writer.endTag(); //closes the Fault element
  }//writeSOAPFault

  /**
   * Serializes an array according to the rules
   * in SOAP 1.1 Section V.
   */
  private void serializeArray(String ctName, ComplexType ct, 
  Object parameterValue, Binding binding) throws SOAPException
  {
    try
    {
        QName qname=getBaseTypeForCTArray(ct);
        if (qname==null)
          return;
          
        QName nonArrayQName=new QName(qname.getNamespaceURI(), 
        qname.getLocalPart().substring(0,qname.getLocalPart().indexOf("[")));
        writer.startElement(ctName);  // write the complexType Name.
        writer.attribute("type", Constants.SOAP_SCHEMA_INSTANCE, 
        "Array", Constants.SOAP_ENCODING_STYLE);
        writer.attribute("arrayType", Constants.SOAP_ENCODING_STYLE,
        qname.getLocalPart(), qname.getNamespaceURI());

        //Start serializing each element of the array.
        if (parameterValue!=null && parameterValue.getClass().isArray())
        {
          int arrayLength=Array.getLength(parameterValue);
          //Object[] oArray=(Object[])parameterValue;
          for (int i=0; i<arrayLength; i++) 
            writeArrayElement(Array.get(parameterValue,i),nonArrayQName);
        }
        writer.endTag(); //end the complexType Name.
    } catch (Exception e)
    {
      throw new SOAPException (e.getMessage());
    }
  }//serializeArray

  private void writeArrayElement(Object aElement, QName dataType) 
  throws Exception
  {
      if (aElement!=null)
      {
        /**
         * Do no use the dataType to lookup the typemap.  This is 
         * because the datatype might be ur-type
         */
         //In the cache the key is the aElement.getclass().getName().
         //The value is an array of Object[] that has:
         //0 - Type, the TypeReference of the parameter class.
         //1 - TypeSerializer - the ComplexTypeSerializer.
        String tmrMap[] = tmr==null?null:tmr.getInfoForClass(aElement.getClass().getName());
        if (tmrMap!=null)
        {
          //For the complexType, get the actual complextype definition
          Type t=getTypeFromReference(new TypeReference(tmrMap[0], tmrMap[1]));
          TypeSerializer serializer=super.getTypeSerializerFromCache(tmrMap[2]);
          if (serializer==null)
          {
            Class serializerClass = Class.forName(tmrMap[2]);
            serializer=(TypeSerializer) serializerClass.newInstance();
            setTypeSerializerCache(tmrMap[2], serializer);
          }
          serializer.marshall(null,"item",writer,t,
          aElement,aElement.getClass(),tmr,this,wsdlHolder);
        }
        else if (aElement instanceof java.util.Date)
         super.serializeDate(null, "item", new XSDType(new QName(Constants.SOAP_SCHEMA,"dateTime")),aElement);
        else 
          //XSD Type
          super.serialize("item", this.getXSDType(aElement),aElement,true);
      }
      else
      {
        //This is a null. write it.
        writer.startElement("item");
        writer.attribute("nil", Constants.SOAP_SCHEMA_INSTANCE, "true", null);
        writer.attribute("type", Constants.SOAP_SCHEMA_INSTANCE,
        dataType.getLocalPart(),dataType.getNamespaceURI());
        writer.endTag();
      }
  }//writeArrayElement

  /**
   * Given a complexType that represents an CT array,
   * returns the based type.

  private QName getBaseTypeForCTArray(ComplexType ct)
  {

     QName baseType=null;
     ComplexContent cc = (ComplexContent)ct.getContent();
     if (cc!=null) 
     {
       Vector v = cc.getContentList();
       if (v!=null) 
       {
         for (int i=0; i<v.size(); i++) 
         {
           if (v.elementAt(i) instanceof Attribute)
           {
             Attribute a = (Attribute)v.elementAt(i);
             Hashtable ht=a.getAdditionalAttributes();
             if (ht!=null)
             {
               Enumeration theKeys=ht.keys();
               Enumeration theValues=ht.elements();
               while (theKeys.hasMoreElements())
               {
                 Object o = theKeys.nextElement();
                 Object value=theValues.nextElement();
                 if (o instanceof QName &&
                 ((QName)o).getNamespaceURI().equals(Constants.WSDL_NAMESPACE) &&
                 ((QName)o).getLocalPart().equals("arrayType"))
                 {
                   baseType=(QName)value;
                   break;
                 }
               }//while
             }
           }
         }//for
       }
     }
     return baseType;
  }//getBaseTypeForCTArray
   */
  private XSDType getXSDType(Object parameterValue)
  {
    if (parameterValue==null)
      return new XSDType(new QName(Constants.SOAP_SCHEMA, "anyType"));
    else if (parameterValue instanceof java.lang.String)
      return new XSDType(new QName(Constants.SOAP_SCHEMA, "string"));
    else if (parameterValue instanceof java.lang.Integer)
      return new XSDType(new QName(Constants.SOAP_SCHEMA, "int")); 
    else if (parameterValue instanceof java.lang.Byte)
      return new XSDType(new QName(Constants.SOAP_SCHEMA, "byte"));
    else if (parameterValue instanceof java.lang.Short)
      return new XSDType(new QName(Constants.SOAP_SCHEMA, "short"));
    else if (parameterValue instanceof java.lang.Long)
      return new XSDType(new QName(Constants.SOAP_SCHEMA, "long"));
    else if (parameterValue instanceof java.lang.Float)
      return new XSDType(new QName(Constants.SOAP_SCHEMA, "float"));
    else if (parameterValue instanceof java.lang.Double)
      return new XSDType(new QName(Constants.SOAP_SCHEMA, "double"));
    else if (parameterValue instanceof java.math.BigDecimal)
      return new XSDType(new QName(Constants.SOAP_SCHEMA, "decimal"));
    else if (parameterValue instanceof java.lang.Boolean)
      return new XSDType(new QName(Constants.SOAP_SCHEMA, "boolean"));
    else if (parameterValue instanceof com.wingfoot.soap.encoding.Base64)
      return new XSDType(new QName(Constants.SOAP_SCHEMA, "base64Binary"));
    else if (parameterValue instanceof com.wingfoot.soap.encoding.HexBinary)
      return new XSDType(new QName(Constants.SOAP_SCHEMA, "hexBinary"));
    else
      return new XSDType(new QName(Constants.SOAP_SCHEMA, "anyType"));
  }
}//class