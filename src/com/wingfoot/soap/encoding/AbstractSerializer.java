package com.wingfoot.soap.encoding;
import com.wingfoot.soap.*;
import com.wingfoot.*;
import com.wingfoot.wsdl.*;
import com.wingfoot.xml.schema.*;
import com.wingfoot.wsdl.soap.*;
import com.wingfoot.xml.schema.types.*;
import org.kxml.Xml;
import org.kxml.io.*;
import org.kxml.parser.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * Converts Java objects to XML stubs that are then
 * part of the SOAP payload.  All serializes, including
 * custom serializers are subclasses of AbstractSerializer.
 * Method in this class are utility methods that convert
 * specific objects to XML stubs.  Concrete implementations
 * of this class address serializations based on Section V
 * encoding rules or literal encoding rules.
 */
public abstract class AbstractSerializer 
{
  WSDLHolder wsdlHolder;
  XMLWriter writer;
  XmlParser parser;

  /**
   * Default constructor.  Takes a WSDLHolder and XMLWriter.
   * @param wsdlHolder the WSDLHolder that contains the
   * WSDL for the service.
   * @param writer XMLWriter where the XML stubs are written
   * to.
   */
  public AbstractSerializer(WSDLHolder wsdlHolder, XMLWriter writer)
  {
    this.wsdlHolder=wsdlHolder;
    this.writer=writer;
  }

  public AbstractSerializer(WSDLHolder wsdlHolder, XmlParser parser)
  {
    this.wsdlHolder=wsdlHolder;
    this.parser=parser;
  }

  /**
   * Writes a parameter as a XML.  The type of the parameter is 
   * an inbuilt XML schema.
   * @param parameterName the name of the parameter.
   * @param type the XSDType that encapsulates the inbuilt XML schema type.
   * @param parameterValue the value of the parameter.  Primitive type are
   * wrapped.
   * @param isNillable true if xsi:nil=true is to be written if the
   * parameterValue is null; false otherwise.
   * @throws SOAPException if the XSDType does not encapsulate an inbuilt
   * XML schema type.
   */
  public void serialize(String parameterName, XSDType type, Object parameterValue,
  boolean isNillable) throws SOAPException 
  {
    this.serialize(parameterName,null,type,parameterValue,isNillable);
  }//serialize

   /**
   * Overriden method that writes a parameter as a XML.  
   * The type of the parameter is an inbuilt XML schema.
   * @param parameterName the name of the parameter.
   * @param parameterNS the namespace associated with the parameter.
   * @param type the XSDType that encapsulates the inbuilt XML schema type.
   * @param parameterValue the value of the parameter.  Primitive type are
   * wrapped.
   * @param isNillable true if xsi:nil=true is to be written if the
   * parameterValue is null; false otherwise.
   * @throws SOAPException if the XSDType does not encapsulate an inbuilt
   * XML schema type.
   */
  public void serialize(String parameterName, String parameterNS, XSDType type,
  Object parameterValue, boolean isNillable) throws SOAPException 
  {
    if (!this.isTypeInbuilt(type.getName()))
      throw new SOAPException("ERROR_SOAPSERIALIZER_001:"+Constants.ERROR_SOAPSERIALIZER_001);

    if (type.getTargetNamespace()==Constants.SOAP_SCHEMA &&
    (type.getName().equals("date")||type.getName().equals("dateTime")))
    {
      serializeDate(parameterNS, parameterName, type,parameterValue);
      return;
    }
    if (parameterNS!=null)
      writer.startElement(parameterName, parameterNS);
    else
      writer.startElement(parameterName);
    if (type!=null)
      writer.attribute("type", Constants.SOAP_SCHEMA_INSTANCE,type.getType(),type.getTargetNamespace());
      
    //Write the element body.  If parameterValue is null and isNillable then
    //just write xsi:nill=true;
    if (isNillable && parameterValue==null)
      writer.attribute("nil", Constants.SOAP_SCHEMA_INSTANCE,"true", null);
    else if (parameterValue!=null)
      writer.elementBody(parameterValue+"");
    writer.endTag();
  }//serialize

  /**
   * Determines if the XSDType encapsulates an inbuilt schema type.
   * Does not check for all the inbuilt types but only the types 
   * that are applicable to the serialize class.
   * @param type String to interrogate.  It contains (int, string) etc.
   * @return true if the xsd type is inbuilt; false otherwise.
   */
  private boolean isTypeInbuilt(String aType) 
  {
    if (aType!=null)
    {
      if (aType.equals("string") ||
      aType.equals("byte") ||
      aType.equals("short") ||
      aType.equals("int") ||
      aType.equals("integer") ||
      aType.equals("long") ||
      aType.equals("float") ||
      aType.equals("double") ||
      aType.equals("decimal") ||
      aType.equals("base64") ||
      aType.equals("base64Binary") ||
      aType.equals("boolean") ||
      aType.equals("hexBinary") ||
      aType.equals("ur-type") ||
      aType.equals("anyType"))
        return true;
    }
    return false;
  }//isTypeInbuilt

  /**
   * Given an element, determines if the element should be
   * namespace prefixed while writing the payload. The 
   * namespace is the targetNamespace of the schema. 
   * The rules to determine this is as follows:
   * <li> If the element is a global element (immediate parent
   * is a &lt;schema&gt;) then the element is namespace qualified.
   * <li> If the element is a local element (immediate parent
   * is NOT a &lt;schema&gt;) but the form attribute in the 
   * element is true, then it is namespace qualified.
   * <li> if the element is a local element (immediate parent
   * is NOT a &lt;schema&gt;) and the form attribute in the element
   * is false but the elementFormDefault attribute in the 
   * &lt;schema&gt; is true, then the element is namespace qualified.
   * @param e the Element to interrogate.
   * @return boolean true if the element is to be namespace prefixed;
   * false otherwise.
   */
  public boolean shouldElementBeNamespaceQualified(Element e) 
  {
    SchemaHolder sArray[]=wsdlHolder.getAllType();
    if (e.isScopeGlobal() || e.isFormQualified())
      return true;
      
    if (sArray!=null) 
    {
      for (int i=0; i<sArray.length; i++) 
      {
        SchemaHolder sh=sArray[i];
        if (sh.getTargetNamespace().equals(e.getNamespace())&&
        sh.isElementQualified()) 
        {
          return true;
        }
      }//for
    }//if
    return false;
  }//shoulldElementBeNamespaceQualified

  /**
   * Determines if a Part must be namespace prefixed in
   * the SOAP payload.  If the Part encapsulates a Type
   * then it is not namespace qualified.  If the Part
   * encapsulates an Element, then the rules governing
   * them are described in the method shouldElementBeNamespaceQualfied.
   * @param part Part to interrogate.
   * @return String the namespace of the Part; null if the
   * Part should NOT be namespace qualified.
   */
  public String getNamespaceForPart(Part part) 
  {
    if (part.getPartType()==Part.TYPE)
      return null;
    Element e = wsdlHolder.getElement(part.getType().getTargetNamespace(),
    part.getType().getName());
    if (this.shouldElementBeNamespaceQualified(e))
      return e.getNamespace();
    else
      return null;
  }

  /**
   * Given an attribute, determines if the attribute should be
   * namespace prefixed while writing the payload. The 
   * namespace is the targetNamespace of the schema. 
   * The rules to determine this is as follows:
   * <li> If the attribute is a global attribute (immediate parent
   * is a &lt;schema&gt;) then the attribute is namespace qualified.
   * <li> If the element is a local attribute (immediate parent
   * is NOT a &lt;schema&gt;) but the form attribute in the 
   * attribute is true, then it is namespace qualified.
   * <li> if the attribute is a local attribute (immediate parent
   * is NOT a &lt;schema&gt;) and the form attribute in the attribute
   * is false but the attributeFormDefault attribute in the 
   * &lt;schema&gt; is true, then the element is namespace qualified.
   * @param attribute the Attribute to interrogate.
   * @return boolean true if the attribute is to be namespace prefixed;
   * false otherwise.
   */
  public boolean shouldAttributeBeNamespaceQualified(Attribute attribute) 
  {
    SchemaHolder sArray[]=wsdlHolder.getAllType();
    if (attribute.isScopeGlobal() || attribute.isFormQualified())
      return true;
      
    if (sArray!=null) 
    {
      for (int i=0; i<sArray.length; i++) 
      {
        SchemaHolder sh=sArray[i];
        if (sh.getTargetNamespace().equals(attribute.getNamespace())&&
        sh.isAttributeQualified()) 
        {
          return true;
        }
      }//for
    }//if
    return false;
  }//shouldAttributeBeNamespaceQualified

  /**
   * Given a message Part, returns the parameterName that must
   * go in the SOAP payload.  If the Part contains a Type as
   * its xsi:type, then the part name is the parameter name. If
   * the Part contains an Element as its xsi:type, then the part
   * name is the Element name.
   * @param part the Part name.
   * @return String the parameter name; returns null if the
   * input part is null.
   */
  public String getParameterNameForPart(Part part) 
  {
    if (part==null)
      return null;
    if (part.getPartType()==Part.TYPE)
      return part.getPartName();
    else 
      return ((TypeReference)part.getType()).getName();
  }

  /**
   * Takes a Part and determines if the Part
   * and returns back the Type of the Part.
   * @param part the Part
   * @return Type of the Part.  It could be
   * a XSDType, a SimpleType or a ComplexType.
   * It is never a TypeReference.  Returns
   * null if the Type cannot be determined.
   */
  public Type getPartType(Part part) 
  {
    if (part==null) 
      return null;
    Type returnType=null;
    if (part.getPartType()==Part.TYPE) 
    {
      returnType= getTypeFromReference(part.getType());
    }//if TYPE
    else if (part.getPartType()==Part.ELEMENT) 
    {
      TypeReference tr=part.getType();
      Element e = wsdlHolder.getElement(tr.getTargetNamespace(),tr.getName());
      if (e.getType() instanceof TypeReference)
        returnType=getTypeFromReference((TypeReference)e.getType());
      else
        returnType=e.getType();
    }
    return returnType;
  }//getPartType

  /**
   * Takes a TypeReference and returns back a Type that
   * is refered to by the TypeReference.
   * This method is called from getPartType and only
   * works if the TypeReference is pointing to a Type
   * (and not an Element).
   * @param tr the TypeReference.
   * @return Type that is being refered to; null if no
   * Type exists.
   */
  public Type getTypeFromReference(TypeReference tr) 
  {
    if (tr==null)
      return null;
    if (tr.getTargetNamespace().equals(Constants.SOAP_SCHEMA)) 
    {
      if (this.isTypeInbuilt(tr.getLocalPart())||
      tr.getLocalPart().equals("date") ||
      tr.getLocalPart().equals("dateTime")||
      tr.getLocalPart().equals("ur-type") ||
      tr.getLocalPart().equals("anyType")) 
      {
        return new XSDType(new QName(tr.getTargetNamespace(),tr.getLocalPart()));
      }
    }
    SchemaHolder sArray[]=wsdlHolder.getAllType();
    if (sArray==null || sArray.length==0)
      return null;
    for (int i=0; i<sArray.length; i++) 
    {
      if (sArray[i].getTargetNamespace().equals(tr.getTargetNamespace())) 
      {
        for (int j=0; j<sArray[i].getComponentCount(); j++) 
        {
          Component c = sArray[i].getComponent(j);
          if (c!=null && c instanceof Type &&
          ((Type)c).getName().equals(tr.getName()))
            return (Type)c;
        }//for j
      }//if the targetNamespace is equal
    }//for
    return null;
  }//gettypeFromReference

  /**
   * Converts a Date to XML.
   * @param parameterNS the namespace of the parameter; null
   * if the element must not have a namespace.
   * @param parameterName the name of the parameter.
   * @param partType XSDType encapsulating xsd:date or xsd:dateTime
   * @param parameterValue the Date object.
   */
  public void serializeDate(String parameterNS, String parameterName,
  XSDType partType, Object parameterValue) 
  {
    if (parameterNS==null)
      writer.startElement(parameterName);
    else
      writer.startElement(parameterName,parameterNS);
      
    writer.attribute("type",Constants.SOAP_SCHEMA_INSTANCE,partType.getName(),
    partType.getTargetNamespace());
    if (parameterValue==null) 
    {
      writer.attribute("nil",Constants.SOAP_SCHEMA_INSTANCE, "true",null);
    }
    else 
    {
      Date d = new Date();
      Date originalDate = (Date) parameterValue;
      Calendar cal = Calendar.getInstance();
      cal.setTime(originalDate);
      TimeZone tz = cal.getTimeZone();
      int offset = tz.getOffset(
				1,
				cal.YEAR,
				cal.MONTH+1,
				cal.DAY_OF_MONTH,
				cal.DAY_OF_WEEK,
				01);
      if (tz.inDaylightTime(originalDate)) 
      {
        offset=offset<1?offset+3600000:offset-3600000;
      }

      d.setTime(originalDate.getTime() - offset);
      cal = Calendar.getInstance();
      cal.setTime(d);
      StringBuffer sb = new StringBuffer();
      sb.append(cal.get(cal.YEAR)+"-" +
      padInteger((cal.get(cal.MONTH))+1) + "-" +
      padInteger(cal.get(cal.DAY_OF_MONTH)) + "T"+
      padInteger(cal.get(cal.HOUR_OF_DAY))+":"+
      padInteger(cal.get(cal.MINUTE))+":"+
      padInteger(cal.get(cal.SECOND))+"Z");
      writer.elementBody(sb.toString());
    }//else
      writer.endTag();
  }//serializeDate

   /**
   * Utility method to return a two digited string
   * representation for a int.
   * @since 0.90
   * @param value the integer for which a two digited
   * string representation is needed.
   * @return String representation of the int.
   */
  private String padInteger(int value) 
  {
    return value < 10 ? "0"+value : value+"";
  }

  /**
   * Converts a String representation of Date to
   * java.util.Date.
   * @param strDate the String representation of
   * Date.  Usually the String representation is
   * represented as a xsd:date element in a SOAP
   * payload.
   * @return java.util.Date the xsd:date in Date format.
   */
  public java.util.Date deserializeDate(String strDate)
  {
    if (strDate==null)
      return null;
    Date theDate = null;
    //ParseEvent event = parser.read();
    //String elementName=event.getName().trim();
    //while (true) 
    //{
      //event = parser.read();
      //if (event.getType()==Xml.END_TAG && event.getName().trim().equals(elementName))
        //break;
      //else if (event.getType()==Xml.TEXT) 
      //{
        //String strDate=event.getText();
        Calendar cal = Calendar.getInstance();

        cal.set(cal.YEAR, Integer.parseInt(strDate.substring(0,4)));
        cal.set(cal.MONTH, (Integer.parseInt(strDate.substring(5,7))-1));
        cal.set(cal.DAY_OF_MONTH, Integer.parseInt(strDate.substring(8,10)));
        cal.set(cal.HOUR_OF_DAY, Integer.parseInt(strDate.substring(11,13)));
        cal.set(cal.MINUTE, Integer.parseInt(strDate.substring(14,16)));
        cal.set(cal.SECOND, Integer.parseInt(strDate.substring(17,19)));
        int offset=0; 
        if (strDate.endsWith("Z"))  
        {
          cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        else if (strDate.indexOf('-',11) != -1 || strDate.indexOf('+',11) != -1) 
        {
          int theIndex = strDate.indexOf('+', 11) == -1 ?
          strDate.indexOf('-', 11) :
          strDate.indexOf('+', 11);
          String offsetHour = strDate.substring(theIndex+1, theIndex+3);
          String offsetMinutes = strDate.substring(theIndex+4);
          offset = (Integer.parseInt(offsetHour)*3600000 +Integer.parseInt(offsetMinutes)*60000);
          if (strDate.indexOf('-',11) != -1)
            offset = 0-offset;
          cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        } //else
        theDate = new Date(cal.getTime().getTime()-offset);
      //}
    //} //while true
    return theDate;
  } /* deserializeDate */

  /**
   * Takes a Vector and returns back the elements of
   * the Vector as an array.  If the Vector is 
   * hetrogeneous, then an Object[] is returned; if the
   * array is homogenous, then the suitable array (String[],
   * Integer[] as the case may be) is returned.  If Vector
   * is null or the size is 0, then null is returned.
   * @param v a Vector of actual array values.
   * @param dataTypes the data type of the data in the first
   * parameter.  This is necessary because the individual element
   * in the first parameter may be null.
   */
  public Object getArrayFromVector(Vector v, Vector dataTypes)
  throws ClassNotFoundException
  {
      if (v==null || v.size()==0)
        return null;
      Object theArray=null;
      if (this.isVectorHetrogeneous(dataTypes))
        theArray=Array.newInstance(Class.forName("java.lang.Object"), v.size());
      //temporary fix for version 1.1
      //else if (v.elementAt(0) ==null)
        //return null;
      else
        //theArray=Array.newInstance(v.elementAt(0).getClass(), v.size());
        theArray=Array.newInstance(((Class)dataTypes.elementAt(0)), v.size());
      for (int i=0; i<v.size(); i++)
        Array.set(theArray,i,v.elementAt(i));
      return theArray;
  }

    /**
   * Determines if a given Vector is hetrogeneous.  An
   * Array is hetrogeneous if any element of the Vector
   * belongs to a different class that any other
   * element of the Vector.
   * Each element of the Vector contains an instance of Class.
   * @param Vector the array to interrogate.
   * @return true if the array is hetrogeneous, false
   * if not.
   */
  private boolean isVectorHetrogeneous(Vector v) {
        
    if (v==null)
      return true; //treat null arrays as hetrogenous

    boolean returnValue=false;
    Object previousElement=null;
    for (int i=0; i<v.size(); i++) 
    {
      Object o= v.elementAt(i);
      if (i!=0 && o != null &&  
      (! o.toString().equals(previousElement.toString()))) 
      {
          returnValue=true;
          break;
      } //else
      previousElement=o;
    } //for
    return returnValue;
  } /* isArrayHetrogeneous */

  /**
   * Takes an instance of XmlParser and reads
   * it until a START_TAG is encountered or 
   * the end of the Xml Document is encountered
   * @param parser the Xml document encapsulated
   * as an XmlParser.
   * @throws IOException if an error occurs while
   * reading the parser.
   */
  public  void skipTillStartTag(XmlParser parser)
  throws IOException
  {
    if (parser==null)
      return;
    while (true) 
    {
      ParseEvent pe = parser.peek();
      if (pe.getType()==Xml.END_DOCUMENT || pe.getType()==Xml.START_TAG)
        break;
      else
        parser.read();
    }
  }

 /**
   * Gets namespace for an operation. In a WSDL, the namespace
   * is available as part of the binding operation.  It identifies
   * the namespace for the &lt;input&gt; and &lt;output&gt; messages.
   * 
   * Only applicable for RPC operation.  Exception thrown if the operation is 
   * not found in Binding
   */
  public String getNamespaceForOperation(Binding binding, Operation operation, Message message)
  throws SOAPException
  {
    //BindingOperation bo=this.getBindingOperation(binding,operation);
    BindingOperation bo=binding.getBindingOperation(operation);
    MessageFormat[] mf=null;
    if (operation.getInputMessage().equals(message))
       mf =bo.getInputMessageFormat();
    else if (operation.getOutputMessage().equals(message))
       mf =bo.getOutputMessageFormat();
       
    if (mf!=null) 
    {
      if (mf[0] instanceof SOAPMessage) 
        return ((SOAPMessage)mf[0]).getNamespaceURI();
    }
    return "";
  }//getNamespaceForOperation
  
  protected QName getBaseTypeForCTArray(ComplexType ct)
  {
    /**
     * Determine the base type of the array.
     */
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
  
  /**
   * Utility method that takes a Type and returns a Class
   * that encapsulates the Java data type of the input type. 
   * @param type
   * @return 
   */
  public Class getJavaType(Type type, TypeMappingRegistry tmr)
  throws ClassNotFoundException
  {
    Class clazz=null;
    if (type ==null)
      return null;
    //First resolve the TypeReference
    if (type instanceof TypeReference)
      type = this.getTypeFromReference((TypeReference)type);
    //Now resolve SimpleType
    if (type instanceof SimpleType)
    {
      String st = type.toString();
      String ns=st.substring(0,st.indexOf(':')+1);
      String name=st.substring(st.indexOf(':')+1);
      type = new XSDType(new QName(ns,name));
    }
    //The only two possibilities left are ComplexType and XSDType.
    if (type instanceof ComplexType) 
    {
      ComplexType ct = (ComplexType) type;
      String name = ct.getName();
      String ns = ct.getTargetNamespace();
      String[] info = tmr.getInfoForNamespace(ns, name);
      if (info!=null && info[0]!=null)
        clazz = Class.forName(info[0]);
    }
    else if (type instanceof XSDType)
    {
      XSDType xsdType = (XSDType)type;
      if (xsdType.getName().equals("string"))
        clazz = Class.forName("java.lang.String");
      else if (xsdType.getName().equals("byte"))
        clazz = Class.forName("java.lang.Byte");
      else if (xsdType.getName().equals("short"))
        clazz = Class.forName("java.lang.Short");
      else if (xsdType.getName().equals("int") || xsdType.getName().equals("integer"))
        clazz = Class.forName("java.lang.Integer");
      else if (xsdType.getName().equals("long"))
        clazz = Class.forName("java.lang.Long");
      else if (xsdType.getName().equals("float"))
        clazz = Class.forName("java.lang.Float");
      else if (xsdType.getName().equals("double"))
        clazz = Class.forName("java.lang.Double");
      else if (xsdType.getName().equals("decimal"))
        clazz = Class.forName("java.math.BigDecimal");
      else if (xsdType.getName().equals("base64") ||
      xsdType.getName().equals("base64Binary"))
        clazz = Class.forName("com.wingfoot.soap.encoding.Base64");
      else if (xsdType.getName().equals("boolean"))
        clazz = Class.forName("java.lang.Boolean");
      else if (xsdType.getName().equals("hexBinary"))
        clazz = Class.forName("com.wingfoot.soap.encoding.HexBinary");
      else if (xsdType.getName().equals("ur-type") ||
      xsdType.getName().equals("anyType"))
        clazz = Class.forName("java.lang.Object");      
    }
    return clazz;
  } //getJavaType

}/*class AbstractSerializer*/