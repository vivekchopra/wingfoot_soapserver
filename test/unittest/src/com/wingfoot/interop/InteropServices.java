package com.wingfoot.interop;


import java.util.*;
import java.math.BigDecimal;
import com.wingfoot.soap.*;
import com.wingfoot.soap.encoding.*;
import com.wingfoot.soap.transport.*;
import com.wingfoot.interop.interop.*;

/**
 * This class contains all the methods required for interop.
 * These methods simply return the data that was sent to this
 *  class
 */

public class InteropServices {

    public InteropServices(){
	
    }//empty constructor

    /**
     * This method echos a String value
     */

    public String echoString(String value) {

	 return value;
    }

    /**
     * This method  echos a String array
     * This service is taking an Object array instead 
     * is because Wingfoot SOAP does not preserve type 
     * when deserializing an array
     */

    public String[] echoStringArray(Object[] value) {
        
	if (value==null)
	     return null;

	String[] str = new String[value.length];

	for(int i = 0; i < value.length; i++) {
	    str[i] = (String) value[i];
	}
	return str;
    }

    /**
     * This method is overloaded.  This is also an 
     * echoStringArray method, but takes an Untyped 
     * Object because some soap clients do not sent 
     * xsi:type
     */

    public String[] echoStringArray(UntypedObject uo) {

	String[] str = new String[uo.getPropertyCount()];

	for(int i=0; i < uo.getPropertyCount(); i++) {
	    
	    str[i] = (String) uo.getPropertyValue(i);
	}

	return str;
    }

    public String[] echoStringArray(String str) {
        return null;
    }

    /**
     * This method echos an Integer
     */
    public Integer echoInteger(Integer value) {
	return value;
    }

    /**
     * This method also echos an Integer
     * from a String parameter
     */
    public Integer echoInteger(String value) {
        
	if (value==null)
	     return null;
        else
	     return new Integer(value);
    }

    /**
     * This method echos a Integer Array
     */

    public Integer[] echoIntegerArray(Object[] value) {
        if (value==null)
	     return null;

	Integer[] str = new Integer[value.length];

	for(int i = 0; i < value.length; i++) {
	    str[i] = (Integer) value[i];
	}
	return str;
    }

    /**
     * This method echos a Integer Array
     */
/**
    public Integer[] echoIntegerArray(UntypedObject value) {
        
	if (value==null)
	     return null;

	Integer[] str = new Integer[value.getPropertyCount()];

	for(int i = 0; i < value.getPropertyCount(); i++) {
	    
	    if (value.getPropertyValue(i) instanceof String)
	       str[i] = new Integer((String) value.getPropertyValue(i));
	    else if (value.getPropertyValue(i) instanceof Integer)
	       str[i] = (Integer) value.getPropertyValue(i);
	}
	return str;
    }
    
**/
    public Integer[] echoIntegerArray(String str) {
        return null;
    }


    /**
     * This method echos a Float
     */
    public Float echoFloat(Float value) {
	return value;
    }

    /**
     * This method echos an Float from a 
     * string parameter
     */
    public Float echoFloat(String value) {
	return new Float(value);
    }

    /**
     * This method echos a Float Array
     */

    public Float[] echoFloatArray(Object[] value) {
        if (value==null)
	     return null;

	Float[] str = new Float[value.length];

	for(int i = 0; i < value.length; i++) {
	    str[i] = (Float) value[i];
	}
	return str;
    }

    /**
     * This method echos a Float Array
     */

    public Float[] echoFloatArray(UntypedObject value) {

	Float[] str = new Float[value.getPropertyCount()];

	for(int i = 0; i < value.getPropertyCount(); i++) {
	    if (value.getPropertyValue(i) instanceof String)
	        str[i] = new Float((String)value.getPropertyValue(i));
            else if (value.getPropertyValue(i) instanceof Float)
	        str[i] = (Float)value.getPropertyValue(i);
	}
	return str;
    }

    public Float[] echoFloatArray(String str) {
        return null;
    }

    /**
     * This method takes a structure, in this case 
     * an EmployeeBean and echos it back.
     */

     
    public EmployeeBean echoStruct(EmployeeBean value) {
        if (value==null)
	    return null;
	return this.echoStruct((WSerializable) value);
    }
    
    public EmployeeBean echoStruct(UntypedObject value) {
	return this.echoStruct((WSerializable) value);
    }

    /**
     * This method takes an untyped Object, and return an EmployeeBean 
     */
    public EmployeeBean echoStruct(WSerializable value) {
        
	if (value==null)
	    return null;

	EmployeeBean eb = new EmployeeBean();
        
	for (int i=0; i<value.getPropertyCount(); i++) {
	   if (value.getPropertyName(i).equalsIgnoreCase("varstring"))
	       eb.setName((String)value.getPropertyValue(i));
	   else if (value.getPropertyName(i).equalsIgnoreCase("varInt"))
	       eb.setAge(new Integer(value.getPropertyValue(i)+""));
	   else if (value.getPropertyName(i).equalsIgnoreCase("varFloat"))
	       eb.setSalary(new Float(value.getPropertyValue(i)+""));

	} //for
	return eb;
    }

    /**
     * This method echos a StructArray
     */
    public EmployeeBean[] echoStructArray(Object[] value) {
        if (value==null)
	    return null;

	EmployeeBean[] eb = new EmployeeBean[value.length];

	for(int i = 0; i < value.length; i++) {
	    eb[i] = (EmployeeBean) value[i];
	}
	return eb;
    }

    /**
     * This method echos a StructArray, but the 
     * parameter is an Untyped Object
     */
    public EmployeeBean[] echoStructArray(UntypedObject value) {
        if (value==null)
	    return null;


	EmployeeBean[] eb = new EmployeeBean[value.getPropertyCount()];

	for(int i = 0; i < value.getPropertyCount(); i++) {

	    /**
	     * Each element of the value is an WSerializable.
	     * It might be an EmployeeBean or UntypedObject
	     * but that again extends WSerializable.  
	     * Call either one of the overloaded methods to
	     * deserialize the bean.
	     */
	     eb[i] = echoStruct((WSerializable)value.getPropertyValue(i));
	}
	return eb;
    }

    public EmployeeBean[] echoStructArray(String str) {
        return null;
    }


    /**
     * This method returns a void - essentially nothing
     */
    public void echoVoid() {
	
    }

    /**
     * This method echos a base64 object
     */
    public Base64 echoBase64(byte[] value) {
        if (value==null)
	    return null;
	return new Base64(value);
    }

    /**
     * This method echos a base64 object, 
     * but with a string parameter.  The String
     * contains encoded data
     */
    public Base64 echoBase64(String value) throws SOAPException {
        if (value==null)
	    return null;
	return new Base64(value);
    }

    public Base64 echoBase64(Base64 b) {
        return b;
    }

    /**
     * This method echos a date object
     */
    public Date echoDate(Date value) {
	return value;
    }

    /**
     * This method returns a HexBinary
     */
    public HexBinary echoHexBinary(HexBinary value) {
	return value;
    }

    /**
     * This method returns a HexBinary, but 
     * with String as the parameter.  The
     * String is in encoded form.
     */
    public HexBinary echoHexBinary(String value) throws SOAPException {
        if (value==null)
	    return null;
	return new HexBinary(value);
    }

    /**
     * This method will return a BigDecimal
     */
    public BigDecimal echoDecimal(BigDecimal value) {
	return value;
    }

    /**
     * This method will return a BigDecimal, but with a String as the parameter
     */
    public BigDecimal echoDecimal(String value) {
        if (value==null)
	    return null;
	return new BigDecimal(value);
    }
    
    /**
     * This method will echo a boolean value
     */
    public Boolean echoBoolean(Boolean value) {
	return value;
    }

    /**
     * This method will echo a boolena, but with a String as the parameter
     */
    public Boolean echoBoolean(String value) {
        if (value==null)
	    return null;
	return new Boolean(value);
    }

    /**
     * The following are GroupB services
     */
    
    /**
     * This method takes a simple structure.
     * It returns back the components of the 
     * structure as a scalar.
     */
    public Envelope echoStructAsSimpleTypes(String value) throws SOAPException {
        return null;
    }
  
    public Envelope echoStructAsSimpleTypes(EmployeeBean value) 
           throws SOAPException { 
        if (value==null)
	    return null;
	
	return echoStructAsSimpleTypes((WSerializable)value);
    }
    
    public Envelope echoStructAsSimpleTypes(UntypedObject value) 
           throws SOAPException { 
        if (value==null)
	    return null;
	return echoStructAsSimpleTypes((WSerializable)value);
    }
    

    /**
     * This method takes an UntypedObject and 
     * returns the components of the structure
     * as a scalar.
     */
    public Envelope echoStructAsSimpleTypes(WSerializable value)
        throws SOAPException {
        
	if (value==null)
	     return null;

        Envelope response = new Envelope();
       try {
        for (int i=0; i<value.getPropertyCount(); i++) {
            if (value.getPropertyName(i).equalsIgnoreCase("varInt"))
	         response.setBody("outputInteger",
		   new Integer(value.getPropertyValue(i)+""), Class.forName("java.lang.Integer"));

            else if (value.getPropertyName(i).equalsIgnoreCase("varString"))
	         response.setBody("outputString",
		   new String(value.getPropertyValue(i)+""), Class.forName("java.lang.String"));

            else if (value.getPropertyName(i).equalsIgnoreCase("varFloat"))
	         response.setBody("outputFloat",
		   new Float(value.getPropertyValue(i)+""), Class.forName("java.lang.Float"));
	} 
      } catch (Exception e){}
        return response;
    }

    /**
     * This method echos simple types as a structure
     */
    public EmployeeBean echoSimpleTypesAsStruct(String s, Integer i, Float f){
	EmployeeBean eb = new EmployeeBean();
	eb.setName(s);
	eb.setAge(i);
	eb.setSalary(f);
	return eb;
    }

    /**
     * This method echos a simple type, 
     * but with all strings as parameters
     */
    public EmployeeBean echoSimpleTypesAsStruct(String s, String i, String f){
	EmployeeBean eb = new EmployeeBean();
	eb.setName(s);
	eb.setAge(new Integer(i));
	eb.setSalary(new Float(f));
	return eb;
    }

    /**
     * This method echos a nested structure
     */

     
    public EmployeeBeanBean echoNestedStruct(EmployeeBeanBean value){
        if (value==null)
	    return null;
	return this.echoNestedStruct((WSerializable) value);
    }
    
    public EmployeeBeanBean echoNestedStruct(UntypedObject value){
        if (value==null)
	    return null;
	return this.echoNestedStruct((WSerializable) value);
    }

    /**
     * This method echos a nested structure, 
     * but with an UntypedObject as the input
     */
    public EmployeeBeanBean echoNestedStruct(WSerializable uo) {
        if (uo==null)
	    return null;
	
	EmployeeBeanBean ebb = new EmployeeBeanBean();

	for(int i = 0; i < uo.getPropertyCount(); i++) {

	    if(uo.getPropertyName(i).equalsIgnoreCase("varString"))
		ebb.setName((String)uo.getPropertyValue(i));

	    else if(uo.getPropertyName(i).equalsIgnoreCase("varInt"))
		ebb.setAge( new Integer(uo.getPropertyValue(i)+""));

	    else if(uo.getPropertyName(i).equals("varFloat"))
		ebb.setSalary( new Float(uo.getPropertyValue(i)+""));

	    else if(uo.getPropertyName(i).equals("varStruct")) 
	        ebb.setBean( this.echoStruct((WSerializable) 
		                 uo.getPropertyValue(i)));
	} //for
	return ebb;
    }

    /**
     * This method will echo a array structure
     */

     
    public EmployeeBeanNestedArray echoNestedArray(EmployeeBeanNestedArray value){
        if (value==null)
	    return null;
	return this.echoNestedArray( (WSerializable) value);
    }
    
    public EmployeeBeanNestedArray echoNestedArray(UntypedObject value){
        if (value==null)
	    return null;
	return this.echoNestedArray( (WSerializable) value);
    }

    public EmployeeBeanNestedArray echoNestedArray(String str) {
        if (str==null)
	    return null;
        return null;
    }

    /**
     * This method will echo a nested array, but with an untypedObject as the input
     */
    public EmployeeBeanNestedArray echoNestedArray(WSerializable uo) {
        if (uo==null)
	    return null;
	
	EmployeeBeanNestedArray ebb = new EmployeeBeanNestedArray();

	for(int i = 0; i < uo.getPropertyCount(); i++) {

	    if(uo.getPropertyName(i).equals("varString"))
		ebb.setName((String)uo.getPropertyValue(i));

	    else if(uo.getPropertyName(i).equals("varInt"))
		ebb.setAge( new Integer( uo.getPropertyValue(i)+""));

	    else if(uo.getPropertyName(i).equals("varFloat"))
		ebb.setSalary( new Float( uo.getPropertyValue(i)+""));

	    else if (uo.getPropertyName(i).equalsIgnoreCase("varArray")) {
                if (uo.getPropertyValue(i) instanceof UntypedObject) {
                   ebb.setArray(this.echoStringArray(
		               (UntypedObject)uo.getPropertyValue(i)));
		}
                else { 
                     //Is an Object array
                   ebb.setArray(this.echoStringArray(
		               (Object[])uo.getPropertyValue(i)));
		}
	    }
	} //for
	return ebb;
    }
} //class
