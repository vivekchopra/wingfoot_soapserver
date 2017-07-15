package com.wingfoot.soap.encoding;
import org.kxml.io.*;
import com.wingfoot.*;
import com.wingfoot.soap.*;
import com.wingfoot.wsdl.*;
import com.wingfoot.xml.schema.*;
import com.wingfoot.xml.schema.types.*;
import com.wingfoot.xml.schema.groups.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class ComplexTypeSerializer implements TypeSerializer 
{
  /**
   * Creates a ComplexTypeSerializer.
   */
  Method[] beanMethods=null;
  Field[] beanFields=null;
  
  public ComplexTypeSerializer()
  {
  }
  
  public void marshall(String parameterNS, String parameterName,
  XMLWriter writer, Type type, 
  Object objectToMarshall, Class objectClass, 
  TypeMappingRegistry registry, SOAPSerializer as, 
  WSDLHolder wsdlHolder) throws SOAPException
  {
    //Make the code reentrant
    //beanMethods=null;
    //beanFields=null;
    if (!(type instanceof ComplexType)||(objectToMarshall!=null && !(objectToMarshall instanceof Serializable)))
      throw new SOAPException("ERROR_SOAPSERIALIZER_007:"+Constants.ERROR_SOAPSERIALIZER_007);
    ComplexType ct=(ComplexType)type;

    /**
     * Write the start element.
     */
    if (parameterNS==null)
        writer.startElement(parameterName);
    else
        writer.startElement(parameterName,parameterNS);

     //write xsi:nil if necessary
    if (objectToMarshall==null)
      writer.attribute("nil", Constants.SOAP_SCHEMA_INSTANCE,"true",null);
    else
    {
      try 
      {
        //Get and store all the Methods in the bean.  This is necessary later.
        if (beanMethods==null)
          beanMethods=objectToMarshall.getClass().getMethods();
        //Get all the fields
        if (beanFields==null)
          beanFields=objectToMarshall.getClass().getFields();
      }//try 
      catch (Exception e){}
    }//else

    //Write xsi:type if the complextype is not anonymous
    if (!(ct.isAnonymous()))
      writer.attribute("type", Constants.SOAP_SCHEMA_INSTANCE,
      ct.getName(), ct.getTargetNamespace());

    //This is a good time to write all the attributes.  Get all the attributes
    com.wingfoot.xml.schema.Attribute[] attributeArray=ct.getAllAttributes();
    if (attributeArray!=null && objectToMarshall!=null) 
    {
      for (int i=0; i<attributeArray.length; i++) 
      {
        com.wingfoot.xml.schema.Attribute a = attributeArray[i];
        StringBuffer sb=new StringBuffer("get").append(a.getName().toUpperCase().charAt(0)).
        append(a.getName().substring(1));
        Method aMethod=this.findMethodInBean(sb.toString());
        Field aField=null;
        //If method is null check for public fields
        if (aMethod==null)
          aField=this.findPublicFieldInClass(a.getName());     
        if (aField==null && aMethod==null && a.getUse()==Attribute.REQUIRED)
          throw new SOAPException("ERROR_SOAPSERIALIZER_008:"+Constants.ERROR_SOAPSERIALIZER_008
          +" "+a.getName());
        else if (aMethod!=null)
          writeAttribute(aMethod,a,objectToMarshall,writer,as);
        else if (aField!=null)
          writeAttribute(aField,a,objectToMarshall,writer,as);
      }//for
    }//if
    try
    {
      //The attributes are all written.  Now start writing the properties.
      if (objectToMarshall!=null)
        this.writeBeanProperty(ct,writer,
        wsdlHolder,objectToMarshall,
        objectClass,registry,as);  
      writer.endTag();
    } catch (Exception e)
      {
        throw new SOAPException(e.getMessage());
      }
  }//marshall

  /**
   * Writes an Method getXXX as an attribute.
   */
  private void writeAttribute(Method aMethod, Attribute a, 
  Object objectToMarshall, XMLWriter writer, SOAPSerializer as)
  throws SOAPException
  {
    try 
    {
      String value=aMethod.invoke(objectToMarshall,null)+"";
      if (as.shouldAttributeBeNamespaceQualified(a))
        writer.attribute(a.getName(),a.getNamespace(),value,null);
      else
        writer.attribute(a.getName(),value);
    } catch (Exception e) 
    {
      throw new SOAPException(e.getMessage());
    }
  }//writeAttribute

  /**
   * Writes a Method getXXX as an attribute.
   */
  private void writeAttribute(Field aField, Attribute a,
  Object objectToMarshall, XMLWriter writer, SOAPSerializer as)
  throws SOAPException 
  {
    try 
    {
      String value=aField.get(objectToMarshall)+"";
      if (as.shouldAttributeBeNamespaceQualified(a))
        writer.attribute(a.getName(),a.getNamespace(),value,null);
      else
        writer.attribute(a.getName(),value);
    } catch (Exception e) 
    {
      throw new SOAPException(e.getMessage());
    }
  }//writeAttribute
  
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
   * Utility method to retrieve the propertyName (getXXX)
   * Checks for the existance of a public getXXX field;
   * then checks for the presence of a public field.
   * If either is found, the value of the property is
   * returned as a string; returns null if not found or
   * the value of the property is null
   */
  private String getPropertyValue(Object bean, String propertyName) 
  {
    String returnValue=null;
    try 
    {
      StringBuffer sb=new StringBuffer("get").append(propertyName.toUpperCase().charAt(0)).
      append(propertyName.substring(1));
      Method m = this.findMethodInBean(sb.toString());
      Field f =null;
      if (m==null)
        f=this.findPublicFieldInClass(propertyName);
      if (m!=null)
      {
        Object o= m.invoke(bean,null);
        if (o!=null)
          returnValue=o+"";
      }
      else if (f!=null) 
      {
        Object o=f.get(bean);
        if (o!=null)
          returnValue=o+"";
      }
      return returnValue;
    }//try
    catch (Exception e) 
    {
      return null;
    }
      
  }//getPropertyValue
  
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

  /**
   * Take a bean and convert each property into XML.
   */
  private void writeBeanProperty(ComplexType ct, XMLWriter xmlWriter, WSDLHolder wsdlHolder,
  Object bean, Class beanClass, TypeMappingRegistry tmr,SOAPSerializer baseSerializer) 
  throws Exception
  {
    //If this is an extension, recurse.
    if (ct.getContent() instanceof ComplexContent &&
    ct.getContent().getDerivation()==Content.EXTENSTION) 
    {
        TypeReference tr=ct.getContent().getBaseType();
        ComplexType baseType=this.retrieveComplexTypeForClass(wsdlHolder,tr.getTargetNamespace(),
        tr.getName());
        if (baseType!=null) //An inheritence
          writeBeanProperty(baseType,xmlWriter, wsdlHolder,bean,beanClass,tmr,baseSerializer);
        else
        {
          //Write the bodyValue property
          //Not yet supported for Encoded.
          String propertyValue=this.getPropertyValue(bean,"bodyValue");
          xmlWriter.elementBody(propertyValue);
        }
    }//if extension
    else if (ct.getContent() instanceof SimpleContent) 
    {
      //write the bodyValue property.  That is all that you have to write
      //Not supported for Encoded.
      String propertyValue=this.getPropertyValue(bean,"bodyValue");
      xmlWriter.elementBody(propertyValue);
    }
    //Write the content model. ComplexType Restriction falls here.
    //Since this is encoded use, we do not care for attributes.
    Vector contentList=ct.getContent().getContentList();
    for (int i=0; contentList!=null && i<contentList.size(); i++) 
    {
      Component c = (Component)contentList.elementAt(i);
      if (c instanceof ModelGroup)
        this.writeModelGroup((ModelGroup)c,bean,beanClass,xmlWriter, tmr,wsdlHolder,baseSerializer);
      else if (c instanceof ModelGroupDefinition)
        this.writeModelGroupDefinition((ModelGroupDefinition)c,bean,beanClass,xmlWriter, tmr,wsdlHolder,baseSerializer);
      else if (c instanceof Element)
        this.writeElement((Element)c,bean,wsdlHolder,baseSerializer);
    }
  }//writeBeanProperty

  /**
   * Given a Bean return the ComplexType.  Returns null
   * if the ComplexType does not exist;
   */
  private ComplexType retrieveComplexTypeForClass( WSDLHolder wsdlHolder, String namespace,
  String localPart)
  {
    SchemaHolder[] shArray=wsdlHolder.getAllType();
    if (shArray!=null) 
    {
      for (int i=0; i<shArray.length; i++) 
      {
        ComplexType ct=shArray[i].getComplexType(localPart,namespace);
        if (ct!=null)
          return ct;
      }
    }
    return null;
  }//retrieveComplexTypeForClass

  /**
   * Writes a ModelGroup to XMLWriter. A ModelGroup consists
   * if either <all>, <choice> or <sequence> and below that
   * element.  It can also contain nested ModelGroup
   */
   private void writeModelGroup(ModelGroup mg, Object bean, Class beanClass, 
   XMLWriter xmlWriter, TypeMappingRegistry tmr,WSDLHolder wsdlHolder,
   SOAPSerializer baseSerializer) 
   throws Exception
   {
     if (mg==null || mg.getContent()==null)
      return;
     List l = mg.getContent();
     for (int i=0; i<l.size(); i++) 
     {
       Component c = (Component)l.get(i);
       if (c instanceof Element)
        this.writeElement((Element)c,bean,/*beanClass,xmlWriter,tmr,*/wsdlHolder,baseSerializer);
       else if (c instanceof ModelGroup)
        this.writeModelGroup((ModelGroup)c,bean,beanClass,xmlWriter,tmr,wsdlHolder,baseSerializer);
     }//for
   } //writeModelGroup

  /**
   * Converts a modelGroupDefinition to XMLWriter.  A
   * ModelGroupDefinition consists of only one ModelGroup.
   * However, if could be a reference to someother
   * ModelGroupDefinition.
   */
   private void writeModelGroupDefinition(ModelGroupDefinition mgd, Object bean,
   Class beanClass, XMLWriter xmlWriter, TypeMappingRegistry tmr,WSDLHolder wsdlHolder,
   SOAPSerializer baseSerializer) 
   throws Exception
   {
     if (mgd==null)
      return;
     if (mgd.isReference()) 
     {
       //A reference to some other modelgroupdefinition.
       SchemaHolder sArray[]=wsdlHolder.getAllType();
       if (sArray==null)
        throw new SOAPException("Cannot find a reference to a ModelGroupDefinition: "+mgd.getName());
       for (int i=0; i<sArray.length; i++) 
       {
         SchemaHolder s = sArray[i];
         for (int j=0; s!=null&&j<s.getComponentCount(); j++) 
         {
           if (s.getComponent(j)!=null && s.getComponent(j) instanceof ModelGroupDefinition &&
           ((ModelGroupDefinition)s.getComponent(j)).getName().equals(mgd.getName()) &&
           ((ModelGroupDefinition)s.getComponent(j)).getTargetNamespace().equals(mgd.getTargetNamespace())
           && (!((ModelGroupDefinition)s.getComponent(j)).isReference()))
            mgd=(ModelGroupDefinition)s.getComponent(j);
         }//for
       }//for
     }//if isReference
     if (mgd==null || mgd.isReference())
      throw new SOAPException("Cannot find a reference to a ModelGroupDefinition: "+mgd.getName());
     if (mgd.getContent()!=null) 
     {
       Vector v = mgd.getContent();
       for (int i=0; i<v.size(); i++) 
       {
         if (v.elementAt(i) instanceof ModelGroup) 
         {
           ModelGroup mg=(ModelGroup)v.elementAt(i);
           this.writeModelGroup(mg,bean,beanClass,xmlWriter,tmr,wsdlHolder,baseSerializer);
         }
       }//for
     }//if
   }//writeModelGroupDefinition

  /**
   * Writes a bean property to XMLWriter.  It uses
   * the getXXX methods on the bean to retrieve
   * the properties.
   * @param e Element from WSDL.  The corresponding
   * property from the bean is picked up.
   * @param bean instance of the bean passed by the User
   * @param beanClass Class of the bean.
   * @param xmlWriter XMLWriter to write the XML to.
   */
  private void writeElement(Element e,Object bean, WSDLHolder wsdlHolder,
  SOAPSerializer baseSerializer) 
  throws Exception
  {
    if (e.isReference())
    {
      e=this.getReferredElement(e, wsdlHolder);
      if (e==null)
        throw new SOAPException("Cannot find a Element with ref attribute:" +e.getName());
    }  
    String propertyName=e.getName();
    Object propertyValue=null;
    String methodName=new StringBuffer("get").append(propertyName.toUpperCase().charAt(0)).
    append(propertyName.substring(1)).toString();
    Method m = this.findMethodInBean(methodName);
    if (m!=null) 
    {
      //Method found. Retrieve the value.
        propertyValue=m.invoke(bean,null);
    }
    else
    {
      //Method not found.  Check to see if a public field exists.
      Field f = this.findPublicFieldInClass(propertyName);
      if (f!=null) 
      {
           propertyValue=f.get(bean);
      }
    }
    //baseSerializer.serialize (xmlWriter, tmr, propertyName, propertyValue,null);
    baseSerializer.serializeParameter(e,propertyValue);
  }//writeElement

  /**
   * Takes an element that is a reference to another element
   * and returns back the referred element; returns null if 
   * the referred element cannot be found.
   */
  private Element getReferredElement(Element e, WSDLHolder wsdlHolder) 
  {
    SchemaHolder shArray[] = wsdlHolder.getAllType();
    if (shArray==null)
      return null;
    for (int i=0; i<shArray.length; i++) 
    {
        SchemaHolder s=shArray[i];
        for (int j=0; j<s.getComponentCount(); j++) 
        {
          if (s.getComponent(j) instanceof Element) 
          {
            Element eref = (Element) s.getComponent(j);
            if (eref!=null && eref.getName().equals(e.getName()) &&
            eref.getNamespace().equals(e.getNamespace()) &&
            (!(eref.isReference())))
              return eref;
          }
        }//for j
    }//for i
    return null;
  }
}//class ComplexTypeSerializer