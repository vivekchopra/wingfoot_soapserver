package com.wingfoot.soap.encoding;
import com.wingfoot.*;
import com.wingfoot.soap.*;
import com.wingfoot.wsdl.*;
import com.wingfoot.xml.schema.*;
import com.wingfoot.xml.schema.types.*;
import com.wingfoot.xml.schema.groups.*;
import com.wingfoot.wsdl.soap.*;
import org.kxml.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * Converts a method and its parameters (Java types) to SOAP payload
 * based on the XML schema provided in the WSDL.  This is
 * known as literal encoding.
 */
public class LiteralSerializer extends AbstractSerializer implements SOAPSerializer
{
  private WSDLHolder wsdlHolder;
  private TypeMappingRegistry tmr;
  private XMLWriter writer;
  private LiteralEnvelope envelope;
  private Hashtable typeSerializerCache;

  /**
   * Creates a LiteralSerializer.
   * @param envelope the LiteralEnvelope that contains the
   * WSDLHolder and the parmeters that for the SOAP body.
   * @param tmr the TypeMappingRegistry that contains the
   * mapping between the ComplexType and the corresponding
   * Java class.
   */
  public LiteralSerializer(LiteralEnvelope envelope,TypeMappingRegistry tmr, XMLWriter writer)
  throws SOAPException
  {
    super(envelope.getWSDLHolder(), writer);
    
    if (/*envelope.getWSDLHolder()==null || */writer==null)
      throw new SOAPException("ERROR_SOAPSERIALIZER_004:" + Constants.ERROR_SOAPSERIALIZER_004);
    this.wsdlHolder=envelope.getWSDLHolder();
    this.tmr=tmr;
    this.writer=writer;
    this.envelope=envelope;
  }//constructor

  /**
   * Takes the parameters in the Envelope and converts 
   * them to SOAP payload based on the rules specified
   * in a XML schema.  The XML schema is part of the
   * WSDL and appears under the &lt;types&gt; element.
   * @param portType the PortType from WSDLHolder that 
   * contains the Operation.  A PortType roughly maps
   * to the class name that contains the method represented
   * by the Operation.
   * @param operation Operation that encapsulates the
   * method name.
   * @param parameters to convert to XML based on the
   * rules specified in the XML schema.
   * @return byte[] the SOAP payload as a binary.
   * @throws SOAPException if:
   * <li> the portType or operation is null;
   */
  public byte[] serialize()  throws SOAPException
  {
    try 
    {
        //Create Envelope element
        writer.startElement ("Envelope", Constants.SOAP_NAMESPACE);
        //write the attributes for Envelope element
        envelope.serialize(writer);
        //Create Body Element.
        writeBody();
        writer.endTag(); //ends the Envelope.
        return writer.getPayload("UTF-8");
    } catch (Exception e) 
    {
      throw new SOAPException(e.getMessage());
    }
  }//serializeBody

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
        //writer.endTag(); //close fault
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
          if (envelope.getMessage()!=null)
          {
            Message m = envelope.getMessage();
            Vector v = envelope.getParameter();
            for (int i=0; i<m.getMessagePartCount()&&v!=null;i++)
              this.serializeParameter(m.getMessagePart(i), v.elementAt(i));
          }
          if (isRPC) 
            writer.endTag(); //close the body element.
      }
      writer.endTag(); //end the Body element.
  }//writeBody

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
   * Takes one parameter and its definition in the WSDL
   * and converts it to XML.
   * @param element the Element in WSDL that contains
   * the definition of the parameterValue.
   * @param parameterValue the value of the parameter.
   */
  public void serializeParameter(Element element,Object parameterValue) 
  throws SOAPException
  {
    try 
    {
      if (element.getMaxOccurs()>1 && parameterValue!=null && 
      (!(parameterValue.getClass().isArray()))) 
        throw new SOAPException("ERROR_SOAPSERIALIZER_006:"+Constants.ERROR_SOAPSERIALIZER_006);
      
      //Get the xsi:type in the element.
      Type elementType=element.getType();

      if (elementType!=null && elementType instanceof TypeReference)
        elementType=super.getTypeFromReference((TypeReference)elementType);

      if (elementType==null)
        throw new SOAPException("Cannot find definition of type in Schema:" + element);
      //Serialize an array
      if (elementType instanceof ComplexType
      && ((ComplexType)elementType).isLiteralArray())
      {
        serializeArray(element.getName(),
        super.shouldElementBeNamespaceQualified(element)?element.getNamespace():null, 
        parameterValue, (ComplexType)elementType);
        return;
      }
      //Check for TypeMappingRegistry here
      String[] strarray = tmr != null ? 
      tmr.getInfoForNamespace(elementType.getTargetNamespace(),
      elementType.getName()==null?element.getName():elementType.getName()):null;   
      if (strarray != null) 
      {
        TypeSerializer serializer = null;
        serializer=this.getTypeSerializerFromCache(strarray[0]);
        if (serializer==null)
        {
          Class serializerClass = Class.forName(strarray[1]);
          serializer=(TypeSerializer) serializerClass.newInstance();
          setTypeSerializerCache(strarray[0], serializer);
        }
        String elementNS=element.getNamespace();
        String elementName=element.getName();
        if (!super.shouldElementBeNamespaceQualified(element))
          elementNS=null;
        if (element.getMaxOccurs()>1)
        {	
          Object[] oArray=(Object[])parameterValue;
          for (int i=0; i<element.getMaxOccurs()/*&&i<oArray.length*/;i++)
          {
                  serializer.marshall(elementNS,elementName,writer,elementType,
                  oArray==null?null:oArray[i],Class.forName(strarray[0]),tmr,this,wsdlHolder);
                  if (oArray!=null && i+1==oArray.length)
                    break;
                  else if (oArray==null)
                    break;
          }
        }//if
        else
        serializer.marshall(elementNS,elementName,writer,elementType,
        parameterValue,Class.forName(strarray[0]),tmr,this,wsdlHolder);
      }//if TypeMappingResitry
      else if (elementType instanceof XSDType) 
      {
        this.writeXSDElement(element, (XSDType)elementType,parameterValue);
      }
      else if (elementType instanceof SimpleType) 
      {
        String st = ((SimpleType)elementType).toString();
        String ns=st.substring(0,st.indexOf(':')+1);
        String name=st.substring(st.indexOf(':')+1);
        XSDType xsd=new XSDType(new QName(ns,name));
        this.writeXSDElement(element, xsd,parameterValue);
      }
      else
          throw new SOAPException("ERROR_SOAPSERIALIZER_012:"+
          Constants.ERROR_SOAPSERIALIZER_012+element.getName());
    } catch(Exception e)
    {
      throw new SOAPException(e.getMessage());
    }
  }//serializeParameter(Element)

  private void serializeArray(String ctName, String ctNS,
  Object parameterValue,ComplexType ct)
  throws SOAPException
  {
    //Write the wrapper.
    if (ctNS==null)
      writer.startElement(ctName);
    else
      writer.startElement(ctName, ctNS);
    ComplexContent cc = (ComplexContent)ct.getContent();
    List l = cc.getContentList();
    ModelGroupImplementation mgi=(ModelGroupImplementation)l.get(0);
    List ll = mgi.getContent();
    Element arrayElement=(Element)ll.get(0);

    serializeParameter(arrayElement,parameterValue);

    //End the tag.
    writer.endTag();
  }
  
  /**
   * Writes a primitive java type to XML.  Takes
   * care of Arrays too.
   */
  private void writeXSDElement(Element element, XSDType elementType, Object value) 
  throws SOAPException
  {
    //Is this an array.
    if (element.getMaxOccurs()>1) 
    {
      //Object[] oArray=(Object[])value;
      for (int i=0; i<element.getMaxOccurs(); i++) 
      {
        
        Object arrayElement = value==null? null:Array.get(value, i);
        if (elementType.getName().equals("date") || elementType.getName().equals("dateTime")) 
          super.serializeDate(super.shouldElementBeNamespaceQualified(element)?
          element.getNamespace():null, element.getName(),elementType,arrayElement==null?null:(Date)arrayElement /*oArray[1]*/);
        else if (super.shouldElementBeNamespaceQualified(element))
          super.serialize(element.getName(),element.getNamespace(),elementType,arrayElement==null?null:arrayElement,
          element.isNillable());
        else
          super.serialize(element.getName(),elementType,arrayElement==null?null:arrayElement,element.isNillable());
        if (value!=null && i+1==Array.getLength(value))
          break;
        else if (value==null)
          break;
        }//for 
    }//if array
    else if (elementType.getName().equals("date") || elementType.getName().equals("dateTime")) 
    {
      super.serializeDate(super.shouldElementBeNamespaceQualified(element)?
      element.getNamespace():null, element.getName(),elementType,value);
    }
    else if (super.shouldElementBeNamespaceQualified(element))
      super.serialize(element.getName(),element.getNamespace(),elementType,value,
      element.isNillable());
    else
      super.serialize(element.getName(),elementType,value,element.isNillable());
  }//writeXSDElement

/****************** Code to process the Part **********************/

  /**
   * Takes a Part and the parameter value and converts to 
   * SOAP XML stub.
   * @param part the Part.
   * @param parameterValue the value to serialize.
   */
  public void serializeParameter(Part part, Object parameterValue) 
  throws SOAPException
  {
    try
    {
      if (part.getPartType()==Part.ELEMENT) 
      {
        Element e = wsdlHolder.getElement(part.getType().getTargetNamespace(),
        part.getType().getName());
        this.serializeParameter(e,parameterValue);
      }
      else if (part.getPartType()==Part.TYPE) 
      {
        Type partType=null;
        TypeReference tr=part.getType();
        if (tr!=null) 
          partType=super.getTypeFromReference(tr);
        if (partType==null)
           throw new SOAPException("Cannot find definition of type in Schema:" + tr);

        if (partType instanceof ComplexType &&
        ((ComplexType)partType).isLiteralArray()) 
        {
          ComplexType ct=(ComplexType)partType;
          this.serializeArray(ct.getName(),ct.getTargetNamespace(),parameterValue,ct);
          return;
        }

        //Check for TypeMappingRegistry here
        String[] strarray = tmr != null ? 
        tmr.getInfoForNamespace(partType.getTargetNamespace(),partType.getName()):null;   
        if (strarray != null) 
        {
          TypeSerializer serializer = null;
          serializer=getTypeSerializerFromCache(strarray[0]);
          if (serializer==null)
          {
            Class serializerClass = Class.forName(strarray[1]);
            serializer=(TypeSerializer) serializerClass.newInstance();
            setTypeSerializerCache(strarray[0], serializer);
          }
          serializer.marshall(null,part.getPartName(),writer,partType,
          parameterValue,Class.forName(strarray[0]),tmr,this,wsdlHolder);
        }      
        else if (partType instanceof XSDType) 
        {
          this.writeXSDElement(part.getPartName(),(XSDType)partType,parameterValue);
        }
        else if (partType instanceof SimpleType) 
        {
          String st = ((SimpleType)partType).toString();
          String ns=st.substring(0,st.indexOf(':')+1);
          String name=st.substring(st.indexOf(':')+1);
          XSDType xsd=new XSDType(new QName(ns,name));
          this.writeXSDElement(part.getPartName(),xsd,parameterValue);
        }
        else
          throw new SOAPException("ERROR_SOAPSERIALIZER_012:"+Constants.ERROR_SOAPSERIALIZER_012+partType.getName());
      }//else
    } catch (Exception e) 
    {
      throw new SOAPException(e.getMessage());
    }
  }//serializeParameter(Part, Object)

  /**
   * Writes a primitive java type to XML.  Takes
   * care of Arrays too.
   */
  private void writeXSDElement(String partName, XSDType elementType, Object value) 
  throws SOAPException
  {
    if (elementType.getName().equals("date") || elementType.getName().equals("dateTime")) 
      super.serializeDate(null,partName,elementType,value);
    else
      super.serialize(partName,elementType,value,false);
  }//writeXSDElement

  private XMLWriter createWriter()
  {
    /**
     * Create the XMLWriter.
     */
    Hashtable hashtable = new Hashtable();
    hashtable.put (Constants.SOAP_NAMESPACE,"SOAP-ENV");
    hashtable.put (envelope.getSchemaInstance(), "xsi");
    hashtable.put (envelope.getSchema(), "xsd");
    return new XMLWriter(hashtable);
  }

  /**
   * Stores an instance of TypeSerializer (example ComplexTypeSerializer) into
   * a cache so that an array of such ComplexTypeSerializers need not instantiate
   * the TypeSerializer repeatedly.
   * @param typeSerializerName the name of the class; it is fully packaged as in
   * the String retrieved from typeSerializerName.getClass().getName().
   * @param instance the instance of the TypeSerializer
   */
  public void setTypeSerializerCache(String typeSerializerName, TypeSerializer instance)
  {
    if (typeSerializerCache==null)
      typeSerializerCache=new Hashtable();
    typeSerializerCache.put(typeSerializerName,instance);
  }//setTypeSerializerCache

  /**
   * Retrieves (if present) the instance of the TypeSerializer with the
   * input name from the cache.
   * @param typeSerializerName String with the name of the serializer class.
   * @return TypeSerializer the instance of the TypeSerializer from the
   * cache; null if the key is not present.
   */
  public TypeSerializer getTypeSerializerFromCache(String typeSerializerName)
  {
    if (typeSerializerName==null || typeSerializerCache==null)
      return null;
    return (TypeSerializer) typeSerializerCache.get(typeSerializerName);
  }
}//LiteralSerializer


