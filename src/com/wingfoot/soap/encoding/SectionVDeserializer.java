package com.wingfoot.soap.encoding;
import com.wingfoot.*;
import com.wingfoot.soap.*;
import com.wingfoot.wsdl.*;
import com.wingfoot.xml.schema.*;
import com.wingfoot.xml.schema.types.*;
import org.kxml.parser.*;
import org.kxml.*;
import java.io.*;
import java.util.*;
/**
 * Converts into Java objects,the parameters in a SOAP payload 
 * that are encoded using the rules specified in
 * <a href="http://www.w3.org/TR/SOAP/#_Toc478383512">SOAP 1.1 Section V</a>.
 * A SOAP payload created using Section V rules is popularly refered to as 
 * encoded using Section V encoding.
 */
public class SectionVDeserializer extends LiteralDeserializer 
implements SOAPDeserializer 
{
  private LiteralEnvelope envelope;
  private TypeMappingRegistry registry;
  private XmlParser parser;
  private WSDLHolder wsdlHolder;
  private byte[] payload;
  private HashMap hrefCache=new HashMap();
  /**
   * Creates a SectionVDeserializer.
   * @param envelope LiteralEnvelope where the paramerters represented in 
   * the SOAP payload and converted into Java objects are stored.
   * @param tmr TypeMappingRegistry contains the mapping between ComplexType
   * and Java objects.
   * @param parser XmlParser that encapsulates the SOAP payload.
   * @param payload the SOAP response in binary form.  This is required 
   * to process any hrefs that might be part of the response.
   * @throws SOAPException if the envelope, parser or
   * the WSDLHolder in the envelope is null.
   */
  public SectionVDeserializer(LiteralEnvelope envelope, TypeMappingRegistry tmr,
  XmlParser parser, byte[] payload) throws SOAPException, IOException
  {
    super(envelope,tmr,parser);
    this.envelope=envelope;
    this.registry=tmr;
    this.parser=parser;
    this.wsdlHolder=envelope.getWSDLHolder();
    this.payload=payload;
    //No need to check for null and throw exception as the super class
    //does that.
  }//constructor.

  /**
   * Reads the SOAP payload and converts any parameters into Java objects.
   * If the payload encapsulates a &lt;SOAP Fault&gt;, it is converted into
   * a Fault object.
   * <p>
   * The converted parameters are stored in the Envelope
   * passed in the constructor.
   * <p>
   * The deserializer is able to process payload containing #href.
   * @throws IOException if any error occurs while reading the payload
   * using the parser.
   * @throws WSDLException if any error occurs while reading the WSDL that
   * contains the definition of the payload.
   * @throws SOAPException if any error occurs while processing the input 
   * SOAP payload.
   */
  public void deserialize() throws
  IOException, WSDLException, SOAPException
  {
    boolean isRPC=false;
    //String methodName=null;
    //Get the binding.
    Binding binding=envelope.getWSDLHolder().getBinding(envelope.getPortType());
    isRPC=binding.isOperationRPC(envelope.getOperation());
    //The envelope is of no use.  Skip past it.
    super.skipTillBody();
    //Go in a loop till the </Body> is encountered.
    while (true) 
    {
        ParseEvent pe=parser.peek();
        if (pe.getType()==Xml.END_DOCUMENT ||
        (pe.getType()==Xml.END_TAG && pe.getName().equals("Body")
        && pe.getNamespace().equals(Constants.SOAP_NAMESPACE)))
          break;
        if (pe.getType()==Xml.START_TAG)
        {
        
          if (pe.getName().equals("Fault") &&
          pe.getNamespace().equals(Constants.SOAP_NAMESPACE)) 
          {
            Fault fault = new Fault(parser);
            envelope.setFault(fault);
          }
          else if (isRPC //&& methodName==null 
          && (pe.getAttribute(Constants.SOAP_ENCODING_STYLE,"root")==null ||
          (pe.getAttribute(Constants.SOAP_ENCODING_STYLE,"root")!=null &&
          pe.getAttribute(Constants.SOAP_ENCODING_STYLE,"root").getValue().equals("1"))))
          {
              //This is RPC encoded and pointing to the root.
              //Process the root.
              //methodName=pe.getName();
              deserializeRoot(true);
              break;
          }
          else if (!isRPC
          && (pe.getAttribute(Constants.SOAP_ENCODING_STYLE,"root")==null ||
          (pe.getAttribute(Constants.SOAP_ENCODING_STYLE,"root")!=null &&
          pe.getAttribute(Constants.SOAP_ENCODING_STYLE,"root").getValue().equals("1"))))
          {
              //This is document encoded and pointing to the root.
              //Process the root.
              deserializeRoot(false);
          }

          else //if (methodName==null)
          {
            //This is probably and ID before the root.  Simply skip it.
             //Probably and ID before the root.  Simply skip 
             //It could also be a ID after root.  Either way, skip it.
            String endName=pe.getName();
            String endNS=pe.getNamespace();
            while (true) 
            {
              if (parser.peek().getType()==Xml.END_DOCUMENT ||
              (parser.peek().getType()==Xml.END_TAG &&
              parser.peek().getName().equals(endName) &&
              parser.peek().getNamespace().equals(endNS)))
              {
                parser.read();
                break;
              }
              else
                parser.read();
            }//while
          }//else if methodName
        } //if START_TAG
        else
          //If not a START_TAG, simple keep reading on.
          parser.read();
    }//true
  }//deserialize

  /**
   * Takes the root graph in a Section V SOAP payload and stuffs the
   * return parameter(s) in the Envelope.
   * The payload may be RPC or Document style.  If RPC then
   * skip the wrapper.
   * @param isRPC boolean, true if the payload style is RPC;
   * false otherwise.
   */
  private void deserializeRoot(boolean isRPC) 
  throws IOException, SOAPException
  {
    if (isRPC)
    {
      ParseEvent pe = parser.read();
      super.skipTillStartTag(parser);
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
         /**
          * Call the deserialize(part, parser) in the parent.
          * Because of polymorphism, the deserialize(element, parser)
          * and (type, parser) is called in this class.
          */
         envelope.setParameter(deserialize(p,parser));
       }//for
     }//if
  }//deserializeRoot

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
      Object retValue=null;
      /**
       * If there is any #href, store the hrefID to cache it.
       * If there is a #href, point another instance of the parser
       * to the correct START_TAG.
       */
      String hrefID=isStartTagHref(parser)?getHref(parser):null;
      if (hrefID!=null && hrefCache.containsKey(hrefID))
      {
        parser.read();
        return hrefCache.get(hrefID);
      }
      else if (hrefID!=null)
      {
        XmlParser hrefParser=getParser();
        seekHrefID(hrefParser,hrefID);
        parser=hrefParser;
      }
      if (element.getType() instanceof TypeReference)
        retValue=deserialize(super.getTypeFromReference((TypeReference)element.getType()),parser,element.getName());
      else
        retValue=deserialize(element.getType(),parser,element.getName());

      if (hrefID!=null)
        hrefCache.put(hrefID,retValue);
      return retValue;
  }//deserialize(Element,XmlParser)

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
  throws SOAPException, IOException
  {
      Object returnObject=null;
      /**
       * If there is any #href, store the hrefID to cache it.
       * If there is a #href, point another instance of the parser
       * to the correct START_TAG.
       */
      String hrefID=isStartTagHref(parser)?getHref(parser):null;
      if (hrefID!=null && hrefCache.containsKey(hrefID))
      {
        parser.read();
        return hrefCache.get(hrefID);
      }
      else if (hrefID!=null)
      {
        XmlParser hrefParser=getParser();
        seekHrefID(hrefParser,hrefID);
        parser.read();
        parser=hrefParser;
      }
      
      if (type==null)
        return null;
      else if (type instanceof ComplexType)
      {
          if (((ComplexType)type).isArray())
          {
            //return null to get the class to compile.
            returnObject= this.deserializeArray((ComplexType)type,parser);
          }
          else if (((ComplexType)type).isVector())
          {
            //return null to get the class to compile.
            returnObject= null;
          }
          else if (((ComplexType)type).isHashMap())
          {
            //return null to get the class to compile.
            returnObject= null;
          }
          else
            returnObject= super.deserialize(type,parser,defaultTypeName);
      }
      else
            returnObject= super.deserialize(type,parser,defaultTypeName);
      /**
       * Put the href in the cache
       */
      if (hrefID!=null)
        hrefCache.put(hrefID,returnObject);
      return returnObject;
  }//deserialize

  private Object deserializeArray(ComplexType ct, XmlParser parser)
  throws IOException, SOAPException
  {
      Vector baseTypeVector=new Vector();
      if (ct==null)
      {
        parser.read();
        return null;
      }
      try
      {
        QName qname=super.getBaseTypeForCTArray(ct);
        Type baseType = super.getTypeFromReference(
        new TypeReference(qname.getNamespaceURI(),qname.getLocalPart().endsWith("[]")?
        qname.getLocalPart().substring(0,qname.getLocalPart().indexOf('[')):qname.getLocalPart()));
        Class baseClazz = super.getJavaType(baseType,this.registry);
        if (baseType==null)
          throw new SOAPException("Fatal Error in deserialization. Cannot find baseType for Section V Array");
        String endName=parser.read().getName();
        Vector v = new Vector();
        while (true) 
        {
          ParseEvent pe=parser.peek();
          if (pe.getType()==Xml.END_DOCUMENT || (pe.getType()==Xml.END_TAG &&
          pe.getName().equals(endName)))
          {
            parser.read();
            break;
          }
          else if (pe.getType()==Xml.START_TAG)
          {
              v.add(deserialize(baseType,parser,null));
              baseTypeVector.add(baseClazz);
          }
          else
            parser.read();
        }//while
        return super.getArrayFromVector(v,baseTypeVector);
      } catch (ClassNotFoundException e)
      {
        throw new SOAPException (e.getMessage());
      }
  }//deserializeArray
  
  /**
   * Determines if the START_TAG the XmlParser is
   * pointing to has a #href element.  If the 
   * XmlParser is NOT pointing to a START_TAG, the
   * parser is recursively read until a START_TAG
   * is encountered.
   * @return boolean if the element contains a #href
   * false otherwise.
   */
  private boolean isStartTagHref(XmlParser parser)
  throws IOException
  {
    super.skipTillStartTag(parser);
    ParseEvent pe=parser.peek();
    return pe.getValueDefault("href",null)==null?false:true;
  }//isStartTagHref

  /**
   * Returns the value of the href 
   * (without the prefixed #).
   * @return String the value of the href 
   * parameter.
   */
  private String getHref(XmlParser parser)
  throws IOException
  {
    return parser.peek().getValue("href").substring(1);
  }

  /**
   * Returns a parser for the payload.  This method
   * is used to return a brand new parser to
   * process #href.
   * @return XmlParser parser with the payload.
   */
  private XmlParser getParser() throws IOException
  {
    try
    {
      XmlParser parser = new XmlParser(new InputStreamReader( 
      new ByteArrayInputStream(payload),"UTF-8"));
      return parser;
    } catch (UnsupportedEncodingException e) 
    {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Skips the parser till the id is encountered.
   */
  private void seekHrefID(XmlParser parser, String id)
  throws SOAPException, IOException
  {
    //super.skipTillBody();
    //Skip past the <Body>.
    while (true)
    {
      ParseEvent pe=parser.peek();
      if (pe.getType()==Xml.END_DOCUMENT)
        break;
      else if (pe.getType()==Xml.START_TAG &&
      pe.getName().equals("Body") && pe.getNamespace().equals(Constants.SOAP_NAMESPACE))
      {
        parser.read();
        break;
      }
      else
        parser.read();
    }
    while(true)
    {
      ParseEvent pe = parser.peek();
      if (pe.getType()==Xml.START_TAG &&
      pe.getValueDefault("id", null) !=null &&
      pe.getValueDefault("id", null).equals(id))
        return;
      else if (pe.getType()==Xml.END_DOCUMENT)
        break;
      parser.read();
    }
    throw new SOAPException("Cannot find an id for #href:"+id);
  }
}//class
