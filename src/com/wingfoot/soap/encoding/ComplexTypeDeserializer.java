package com.wingfoot.soap.encoding;
import com.wingfoot.*;
import com.wingfoot.soap.*;
import com.wingfoot.wsdl.*;
import com.wingfoot.xml.schema.*;
import com.wingfoot.xml.schema.types.*;
import org.kxml.Xml;
import org.kxml.parser.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * Converts a ComplexType to a JavaBean.
 */
public class ComplexTypeDeserializer implements TypeDeserializer
{
  Method[] beanMethods=null;
  Field[] beanFields=null;
  
  /**
   * Creates a ComplexTypeDeserializer
   */
  public ComplexTypeDeserializer() 
  {
  }
  /**
   * Entry point to convert a ComplexType to a JavaBean.
   * @param complexType Type that is an instance of ComplexType.
   * The complexType contains the definition of the bean.
   * @param Object the JavaBean where bean parameters are written to.
   * @param parser XmlParser that points to the start tag of the bean.
   * @param deserializer SOAPDeserializer 
   * @param wsdlHolder encausulates the entire WSDL.
   * @throws SOAPException if an error occurs while processing the SOAP
   * payload
   */
  public void unmarshall(Type complexType, Object javaBean, XmlParser parser,
  SOAPDeserializer deserializer, WSDLHolder wsdlHolder) throws SOAPException
  {  
    //Making the beanMethods reentrant.  This is because the complexTypeDeserializers
    //instances are now cached.
    //beanMethods=null;
    //beanFields=null;
    if (complexType instanceof TypeReference)
    {
      SchemaHolder shArray[] = wsdlHolder.getAllType();
      if (shArray!=null)
      {
        for (int i=0; i<shArray.length; i++)
        {
          ComplexType ct = shArray[i].getComplexType(complexType.getName(), complexType.getTargetNamespace());
          if (ct!=null)
          {
            complexType = ct;
            break;
          }
        }
      }
    }
    if (!(complexType instanceof ComplexType))
      throw new SOAPException ("ERROR_SOAPDESERIALIZER_001:" + Constants.ERROR_SOAPDESERIALIZER_001);
    ComplexType ct=(ComplexType) complexType;
    if (ct.getContent()==null)
      return;
    try
    {
        //Get and store all the Methods in the bean.  This is necessary later.
        if (beanMethods==null)
          beanMethods=javaBean.getClass().getMethods();
        //Get all the fields
        if (beanFields==null)
          beanFields=javaBean.getClass().getFields();
        //Process any attributes first.
        Attribute aArray[]=ct.getAllAttributes();
        if (aArray!=null && aArray.length>0)
          deserializeAttributes(aArray, javaBean, parser,deserializer);
        /**
         * Process simplecontent
         */
        if (ct.getContent() instanceof SimpleContent)
          deserializeSimpleContent(ct, javaBean,deserializer,parser);
        else if (ct.getContent() instanceof ComplexContent)
        {
          this.deserializeComplexContent(ct,javaBean,deserializer,parser,wsdlHolder);
        }
          
    } catch (Exception e)
    {
      throw new SOAPException (e.getMessage());
    }
  }//unmarshall

  private void deserializeComplexContent(ComplexType ct, Object javaBean,
  SOAPDeserializer deserializer, XmlParser parser, WSDLHolder wsdlHolder) 
  throws IOException, IllegalAccessException,SOAPException, InvocationTargetException
  {
    String endElement=parser.peek().getName();
    Element eArray[] = ct.getAllElements(wsdlHolder);
    while (true)
    {
      ParseEvent pe = parser.peek();
      if (pe.getType()==Xml.END_DOCUMENT ||
      (pe.getType()==Xml.END_TAG &&
      pe.getName().equals(endElement)))
      {
        //The line below is commented out as a patch for v1.1
        //This was giving problems in LiteralDeserializer for
        //deserializing an array of beans.
        //parser.read();
        break;
      }
      else if (pe.getType()==Xml.START_TAG)
      {
        String elementName=pe.getName();
        Element e = this.getElementFromArray(eArray,elementName);
        if (e!=null) 
        {
          Object value=deserializer.deserialize(e,parser);
          this.populateProperty(elementName,value,javaBean);
        }
        else
          parser.read();
      }
      else
        parser.read();
    }//while
  }//deserializeComplexContent
  
  private void deserializeSimpleContent(ComplexType ct, Object javaBean, 
  SOAPDeserializer deserializer, XmlParser parser) throws IOException, IllegalAccessException,
  SOAPException, InvocationTargetException
  {
    SimpleContent sc = (SimpleContent)ct.getContent();
    Type type=deserializer.getTypeFromReference(sc.getBaseType());
    Object value=deserializer.deserialize(type,parser,null);
    this.populateProperty("bodyValue",value,javaBean);
    
  }//deserializeSimpleContent

  private void deserializeAttributes(Attribute[] aArray, Object javaBean, XmlParser parser,
  SOAPDeserializer sd)  throws IOException, SOAPException, IllegalAccessException,InvocationTargetException
  {
    //Skip till you hit the start tag.
    while (true)
    {
      ParseEvent pe=parser.peek();
      if (pe.getType()==Xml.START_TAG)
        break;
      else if (pe.getType()==Xml.END_DOCUMENT)
        return;
      else
        parser.read();
    }//while
    
    StartTag st=(StartTag)parser.peek();
    Vector attributeVector=st.getAttributes();
    if (attributeVector!=null && attributeVector.size()>0)
    {
      for (int i=0; i<attributeVector.size(); i++)
      {
        Object attributeValue=null;
        org.kxml.Attribute attr = 
        (org.kxml.Attribute) attributeVector.elementAt(i);
        Attribute schemaAttribute=getAttributeFromArray(aArray,attr,sd);
        if (schemaAttribute!=null)
        {
          Type attrType=null;
          if (schemaAttribute.getType() instanceof TypeReference)
            attrType=sd.getTypeFromReference((TypeReference)schemaAttribute.getType());
          else 
            attrType=schemaAttribute.getType();
          attributeValue=sd.deserializeAttribute(attrType,attr.getValue());
        }  
        /**
         * We have the attribute value.  Simply write it 
         * to the bean.
         */
         if (attributeValue!=null)
         {
           this.populateProperty(schemaAttribute.getName(),attributeValue,javaBean);
         }
      }//for
    }//if
  }//deserializeAttributes

  /**
   * Returns an com.wingfoot.xml.schema.type.Attribute from
   * an array of attributes; null if it cannot be found.
   */
  private Attribute getAttributeFromArray(Attribute[] aArray,
  org.kxml.Attribute xmlAttribute, SOAPDeserializer sd)
  {
    if (aArray==null || xmlAttribute==null)
      return null;
    for (int i=0; i<aArray.length; i++)
    {
      boolean includeNS=sd.shouldAttributeBeNamespaceQualified(aArray[i]);
      if (includeNS && aArray[i].getName().equals(xmlAttribute.getName()) &&
      aArray[i].getNamespace().equals(xmlAttribute.getNamespace()))
        return aArray[i];
      else if (!includeNS && aArray[i].getName().equals(xmlAttribute.getName()))
        return aArray[i];
    }//for
    return null;
  }//getAttributeFromArray

    
  /**
   * Returns a Method from a given class.
   * Returns null if the method is not found.
   */
  private Method findMethodInBean(String methodName) 
  {
    if (beanMethods==null)
      return null;
    for (int i=0; beanMethods!=null&& i<beanMethods.length; i++) 
    {
      if (beanMethods[i].getName().equals(methodName))
        return beanMethods[i];
    }//for
    return null;
  }//findMethodInBean

    /**
   * Gets all the public fields in the class.
   * Returns null if there is no such field.
   */
  private Field findPublicFieldInClass(String propertyName) 
  {
    if (this.beanFields==null)
      return null;
    for (int i=0; i<beanFields.length; i++) 
    {
      if (beanFields[i].getName().equals(propertyName))
        return beanFields[i];
    }
    return null;
  }//findPublicFieldInClass

  private void populateProperty(String propertyName, Object attrValue, Object javaBean)
  throws InvocationTargetException, IllegalAccessException
  
  {
    if (attrValue==null)
      return;
    StringBuffer sb=new StringBuffer();
    sb.append("set").append(propertyName.substring(0,1).toUpperCase()).append(propertyName.substring(1));
    
    Method m = this.findMethodInBean(sb.toString());
    Field f=null;
    if (m==null)
      f = this.findPublicFieldInClass(propertyName);
    if (m!=null)
      m.invoke(javaBean,new Object[] {attrValue});
    else if (f!=null)
      f.set(javaBean,attrValue);
  }

  private Element getElementFromArray(Element[] eArray, String elementName) 
  {
    if (eArray==null || elementName==null)
      return null;
    for (int i=0; i<eArray.length; i++)
    {
      Element e = eArray[i];
      if (e.getName().equals(elementName))
        return e;
    }
    return null;
  }

}//class