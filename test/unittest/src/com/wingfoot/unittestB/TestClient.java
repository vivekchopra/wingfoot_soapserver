
package com.wingfoot.unittestB;

import com.wingfoot.soap.*;
import com.wingfoot.soap.encoding.*;
import com.wingfoot.soap.transport.*;
import java.util.*;

public class TestClient {
     
    private String theString;
    private Call theCall;
    private Transport theTransport;
    
    private final String TARGETURI = "urn:echo-unittest"; /*declare the target-uri here, since all tests use the same*/
    
    public TestClient() {
	theTransport = new J2SEHTTPTransport("http://localhost:8080/wingfoot/servlet/wserver", "");
	//theTransport = new J2SEHTTPTransport("http://192.168.2.22:8080/wingfoot/servlet/wserver", "");
    } 

    public static void main (String argv[])  {
	try {
	    TestClient tc = new TestClient();

	    System.err.println("the lenght is: " + argv.length);
	    if(argv.length > 0) {
		if(argv[0].equals("array")) {
		    //System.out.println("Arrays are not working as desired!");
		   // tc.testByteArray();
		   tc.testStringArray();
		    //tc.testIntegerArray();
		    //tc.testFloatArray();
		    //tc.testShortArray();
		    //tc.testDoubleArray();
		}
		else if(argv[0].equals("doublearray")){
		    //tc.testDoubleStringArray();
		    //tc.testDoubleIntegerArray();
		    //tc.testDoubleFloatArray();
		    //tc.testDoubleShortArray();
		    //tc.testDoubleDoubleArray();
		}
		else if(argv[0].equals("vector"))
		    //tc.testVector();
		    tc.testNestedVector();
		else if(argv[0].equals("hashtable"))
		    //tc.testHashtable();
		    tc.testNestedHashtable();
		else if(argv[0].equals("javabean"))
		    //tc.testJavaBean();
		    tc.testAnotherBean();
		else {
		    System.out.println("Usage: TestClient array|doublearray|vector|hashtable|javabean");
		    System.exit(-1);
		}

	    } else {
		System.out.println("Usage: TestClient array|doublearray|vector|hashtable|javabean");
		System.exit(-1);
	    }
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
    }

    public void printFault(Envelope responseEnvelope) {
	Fault f = responseEnvelope.getFault();
	System.err.println(f.getFaultCode()+":"+f.getFaultString());
    }

    public void printResponseInformation(Envelope env) {
	System.err.println("Method Name:" + env.getMethodName());
	System.err.println("Target URI:" + env.getTargetURI());
	System.err.println("Number of parameters:" + env.getParameterCount());
	
	if (env.isFaultGenerated()) 
	    printFault(env);
    }

    /**
     * The String array tests are not quite working as desired.
     * The server is deserializing arrays into Object Arrays and not
     * it's original type.
     * 
     */
    public void testStringArray() throws Exception {
	theCall = new Call();
    /**
	String[] str = {"Hello World",
			new String("temp1"),
			"temp",
			"&%^*",
			"lmn",
			";\"'",
			"Hello          World"};
    **/
        String[] str = new String[] {"Hello World"};
	theCall.addParameter("echoStringArray", str, Class.forName((new String[1]).getClass().getName()));
	//theCall.addParameter("echoStringArray", null, Class.forName((new String[1]).getClass().getName()));
	theCall.setMethodName("echoStringArray");
	theCall.setTargetObjectURI(TARGETURI);

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Object[] response = (Object[]) responseEnvelope.getParameter(0);
	
	for(int i = 0; response!=null &&i < response.length; i++){
	    System.err.println("the String[] response is: " + (String)response[i]);
	}
    }

    /**
     * The String array tests are not quite working as desired.
     * The server is deserializing arrays into Object Arrays and not
     * it's original type.
     * 
     */
    public void testByteArray() throws Exception {
	theCall = new Call();
	byte[] str = "hello".getBytes();
	theCall.addParameter("echoByteArray", str, Class.forName( (new Byte[1]).getClass().getName()));
	theCall.setMethodName("echoByteArray");
	theCall.setTargetObjectURI(TARGETURI);

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	//Base64 response = (Base64) responseEnvelope.getParameter(0);
	//for(int i = 0; i < response.length; i++){
	 //   System.err.println(response[i]);
	//}
	byte[] b = (byte[]) responseEnvelope.getParameter(0);
	System.err.println("***" + new String(b));
    }

    public void testIntegerArray() throws Exception {
         
	theCall = new Call();
	Integer[] i = {new Integer(10000),
		       new Integer(Integer.MAX_VALUE),
		       new Integer(Integer.MIN_VALUE),
		       new Integer(-909090),
		       new Integer(10239),
		       new Integer(-99102)};
	
	theCall.addParameter("echoIntegerArray", i, Class.forName( (new Integer[1]).getClass().getName()));
	theCall.setMethodName("echoIntegerArray");
	theCall.setTargetObjectURI(TARGETURI);

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Object[] response = (Object[]) responseEnvelope.getParameter(0);
	
	for(int j = 0; j < response.length; j++){
	    System.err.println("Test Integer[] " + response[j]);
	}
    }

    

    public void testShortArray() throws Exception {
         
	theCall = new Call();
	Short[] s = {new Short((short)99),
		     new Short(Short.MAX_VALUE),
		     new Short(Short.MIN_VALUE),
		     new Short((short)-99),
		     new Short((short)23),
		     new Short((short)99)};
	theCall.addParameter("echoShortArray", s, Class.forName( (new Short[1]).getClass().getName()));
	theCall.setMethodName("echoShortArray");
	theCall.setTargetObjectURI(TARGETURI);

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Object[] response = (Object[]) responseEnvelope.getParameter(0);

	for(int i = 0; i < response.length; i++){
	    System.err.println("Test ShortArray(): " + response[i]);
	}
    }

    public void testLongArray() throws Exception {
         
	theCall = new Call();
	Long[] l = {new Long(10000000),
		     new Long(Long.MAX_VALUE),
		     new Long(Long.MIN_VALUE),
		     new Long(-10000000),
		     new Long(99999999),
		     new Long(82323444)};
	theCall.addParameter("echoLongArray", l, Class.forName( (new Long[1]).getClass().getName()));
	theCall.setMethodName("echoLongArray");
	theCall.setTargetObjectURI(TARGETURI);

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Object[] response = (Object[]) responseEnvelope.getParameter(0);

	for(int i = 0; i < response.length; i++){
	    System.err.println("Test Long Array: " + response[i]);
	}
    }

    public void testFloatArray() throws Exception {
         
	theCall = new Call();
	Float[] f = {new Float(10.23463523),
		     new Float(Float.MAX_VALUE),
		     new Float(Float.MIN_VALUE),
		     new Float(-10.2334323),
		     new Float(999999.9898),
		     new Float(8232.54345),
		     new Float(Float.NaN),
		     new Float(Float.NEGATIVE_INFINITY),
		     new Float(Float.POSITIVE_INFINITY)};
	theCall.addParameter("echoFloatArray", f, Class.forName((new Float[1]).getClass().getName()));
	theCall.setMethodName("echoFloatArray");
	theCall.setTargetObjectURI(TARGETURI);

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Object[] response = (Object[]) responseEnvelope.getParameter(0);

	for(int i = 0; i < response.length; i++){
	    System.err.println("Test Float Array() :" + response[i]);
	}
	
    }

    public void testDoubleArray() throws Exception {
         
	theCall = new Call();
	Double[] f = {new Double(10.23463523),
		      new Double(Double.MAX_VALUE),
		      new Double(Double.MIN_VALUE),
		      new Double(-10.2334323),
		      new Double(999999.9898),
		      new Double(8232.54345),
		      new Double(Double.NaN),
		      new Double(Double.NEGATIVE_INFINITY),
		      new Double(Double.POSITIVE_INFINITY)};
	theCall.addParameter("echoDoubleArray", f, Class.forName((new Double[1]).getClass().getName()));
	theCall.setMethodName("echoDoubleArray");
	theCall.setTargetObjectURI(TARGETURI);

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Object[] response = (Object[]) responseEnvelope.getParameter(0);
	
	for(int i = 0; i < response.length; i++){
	    System.err.println("Test DoubleArray: " + response[i]);
	}
	
    }


    /**
     * The following are nested arrays
     */

    
    /**
     * The String array tests are not quite working as desired.
     * The server is deserializing arrays into Object Arrays and not
     * it's original type.
     * 
     */
/**
    public void testDoubleStringArray() throws Exception {
	theCall = new Call();
	String[][] str = {{"Hello World",new String("temp1")},
			  {"temp","&%^*"},
			  {"lmn",";\"'"},
			  {"Hello          World", "Hello to Me"}};
	theCall.addParameter("echoTwoStringArray", str);
	theCall.setMethodName("echoTwoStringArray");
	theCall.setTargetObjectURI(TARGETURI);

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Object[] response = (Object[]) responseEnvelope.getParameter(0);
	
	System.err.println("the number of responses is: " + response.length);
	for(int i = 0; i < response.length; i++){
		Object[] o = (Object[]) response[i];
		for(int j = 0; j < o.length; j++){
			System.err.println("the String[][] response is: " + (String)o[j]);
		}

	}
	
    }

   **/ 
    /**
     * This method tests the behavior of Vectors on the SOAP Server
     */
    public void testVector() throws Exception {
         
	theCall = new Call();
	Vector v = new Vector();

	
	v.addElement(new Integer(Integer.MAX_VALUE));
	v.addElement(new Integer(Integer.MIN_VALUE));
	v.addElement(new Long(-909090));
	v.addElement(new Long(10239));
	v.addElement(new Integer(-99102));
	v.addElement(new Integer(29));
	v.addElement(new String("fuck you"));
	
	theCall.addParameter("echoVector", v, Class.forName("java.util.Vector"));
	theCall.setMethodName("echoVector");
	theCall.setTargetObjectURI(TARGETURI);

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Vector response = (Vector) responseEnvelope.getParameter(0);
	System.err.println("**************");
	for( int i = 0; i < response.size(); i++){
	    System.err.println(response.elementAt(i));
	}
	System.err.println("**************");
    }

    /**
     * This test will observe the behavior of nested vector
     */
    public void testNestedVector() throws Exception {
	
	theCall = new Call();
    	Vector innerV = new Vector();
	
	innerV.addElement(new String("good"));
	innerV.addElement(new String("day"));
	innerV.addElement(new Integer(Integer.MAX_VALUE));
	innerV.addElement(new Integer(Integer.MIN_VALUE));
	innerV.addElement(new Long(-909090));

	Vector innerV1 = new Vector();
	
	innerV1.addElement(new String("HELLO"));
	innerV1.addElement(new String("FROM"));
	innerV1.addElement(new String("ME"));
	innerV1.addElement(new Integer(Integer.MAX_VALUE));
	innerV1.addElement(new Integer(Integer.MIN_VALUE));
	innerV1.addElement(new Long(-9999999));

	Vector v = new Vector();
	v.addElement(innerV);
	v.addElement(innerV1);
	
	theCall.addParameter("echoVector", v, Class.forName("java.util.Vector"));
	//theCall.addParameter("echoVector", null, Class.forName("java.util.Vector"));
	theCall.setMethodName("echoVector");
	theCall.setTargetObjectURI(TARGETURI);
	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Vector response = (Vector) responseEnvelope.getParameter(0);
	System.err.println("**************");
	for( int i = 0; i < response.size(); i++){
	    Vector testVect = (Vector)response.elementAt(i);
	    for(int j = 0; j < testVect.size(); j ++){
		System.err.println("******innerVector values*************");
		System.err.println(testVect.elementAt(j));
		System.err.println("******end innerVector values*************");
	    }
	}
	System.err.println("**************");
    }
    
    /**
     * This method will test a Hashtable and it's behavior on our SOAP Server
     */
    public void testHashtable() throws Exception {
         
	theCall = new Call();
	Hashtable v = new Hashtable();
	v.put("number_one", new Integer(Integer.MAX_VALUE));
	v.put("number_one1",new Integer(Integer.MIN_VALUE));
	v.put("number_one2",new Long(-909090));
	v.put("number_one3",new Long(10239));
	v.put("number_one4",new String("good day"));
	
	theCall.addParameter("echoHashtable", v, Class.forName("java.util.Hashtable"));
	theCall.setMethodName("echoHashtable");
	theCall.setTargetObjectURI(TARGETURI);

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Hashtable response = (Hashtable) responseEnvelope.getParameter(0);
	System.err.println("**************");
	for(Enumeration e = response.keys(); e.hasMoreElements();){
	    String index = (String) e.nextElement();
	    System.err.println("The hashtable index is: " + index);
	    System.err.println("The hashtable value is: " + response.get(index));
	}
	System.err.println("**************");
    }

    /**
     * This method will test a Hashtable and it's behavior on our SOAP Server
     * The plan is to add a few SOAP Structs to the hashtable
     *     A regular vector
     *     A nested vector
     *     A Hashtable with a String and a String[]
     */
    public void testNestedHashtable() throws Exception {
         
	theCall = new Call();
	Hashtable v = new Hashtable();

	/**
	 * The vector to be encapsulated in the hashtable
	 */
	Vector vec = new Vector();
	vec.addElement(new Integer(Integer.MAX_VALUE));
	vec.addElement(new Integer(Integer.MIN_VALUE));

	/**
	 * The nested vector to be encapsulated in the hashtable
	 */
	Vector innerV = new Vector();
	innerV.addElement(new String("good"));
	innerV.addElement(new String("day"));
	innerV.addElement(new Integer(Integer.MAX_VALUE));
	innerV.addElement(new Integer(Integer.MIN_VALUE));
	innerV.addElement(new Long(-909090));

	Vector innerV1 = new Vector();
	innerV1.addElement(new String("HELLO"));
	innerV1.addElement(new String("FROM"));
	innerV1.addElement(new String("ME"));
	innerV1.addElement(new Integer(Integer.MAX_VALUE));
	innerV1.addElement(new Integer(Integer.MIN_VALUE));
	innerV1.addElement(new Long(-9999999));

	Vector big = new Vector();
	big.addElement(innerV);
	big.addElement(innerV1);


	/**
	 * Nested Hashtable
	 */

	String[] str = {"Hello World",
			new String("temp1"),
			"temp",
			"&%^*",
			"lmn",
			";\"'",
			"Hello          World"};
	
	Hashtable ht = new Hashtable();
	ht.put("str", new String("Hello world"));
	ht.put("strArray", str);

	
	/**
	 * These are the three data structures added to the hashtable
	 */
	
	v.put("vector", vec);
	v.put("nestedvector", big);
	v.put("nestedHashtable", ht);

	
	theCall.addParameter("echoHashtable", v, Class.forName("java.util.Hashtable"));
	//theCall.addParameter("echoHashtable", null, Class.forName("java.util.Hashtable"));
	theCall.setMethodName("echoHashtable");
	theCall.setTargetObjectURI(TARGETURI);

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);

	/**
	 * This is the response from the server
	 */
	Hashtable response = (Hashtable) responseEnvelope.getParameter(0);
	

	/**
	 * The following printf's are to output what the server returned
	 */
	Vector responseVector = (Vector)response.get("vector");
	
	System.err.println("***vector***");
	for(int i = 0; i < responseVector.size(); i ++){
	    System.err.println("printing out the vector info: " + responseVector.elementAt(i));
	}
	System.err.println("***end vector***\n\n");

	responseVector = (Vector) response.get("nestedvector");
	System.err.println("******nested Vector********");
	for( int i = 0; i < responseVector.size(); i++){
	    Vector testVect = (Vector) responseVector.elementAt(i);
	    for(int j = 0; j < testVect.size(); j ++){
		System.err.println("******innerVector values*************");
		System.err.println(testVect.elementAt(j));
		System.err.println("******end innerVector values*************");
	    }
	}
	System.err.println("*****end nested Vector*********\n\n");

	Hashtable returnHashtable = (Hashtable)response.get("nestedHashtable");
	System.err.println("*****nested Hashtable*****");
	String s0 = (String) returnHashtable.get("str");
	System.err.println("the s0 is: " + s0);
	Object[] sArr = (Object[]) returnHashtable.get("strArray");
	for(int k = 0; k < sArr.length; k ++){
	    System.err.println("the sArr[k] is: " + (String) sArr[k]);
	}
	System.err.println("*****end nested Hashtable*****\n\n");
    }

    /**
     * This method tests the behavior of Java Beans against our SOAP Server
     * I am using the good ol Employee Bean for testing
     */
    public void testJavaBean() throws Exception {

	EmployeeBean eb = new EmployeeBean();
	eb.setName("Baldwin");
	eb.setAge(new Integer(25));
	eb.setSalary(new Float(43.23));

	Envelope requestEnvelope = new Envelope();
	requestEnvelope.setSchema(Constants.SOAP_SCHEMA);
	requestEnvelope.setSchemaInstance(Constants.SOAP_SCHEMA_INSTANCE);
	theCall = new Call(requestEnvelope);
	
	TypeMappingRegistry tmr = new TypeMappingRegistry();
	tmr.mapTypes("http://www.wingfoot.com/xsd",
		     "SOAPStruct",
		     Class.forName("com.wingfoot.unittestB.EmployeeBean"),
		     Class.forName("com.wingfoot.soap.encoding.BeanSerializer"),
		     Class.forName("com.wingfoot.soap.encoding.BeanSerializer"));
		     
	theCall.setMappingRegistry(tmr);
	theCall.addParameter("echoEmployeeBean", eb, Class.forName("com.wingfoot.unittestB.EmployeeBean"));
	theCall.setMethodName("echoEmployeeBean");
	theCall.setTargetObjectURI(TARGETURI);

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	EmployeeBean response = (EmployeeBean) responseEnvelope.getParameter(0);
	System.err.println("**************");
	System.err.println(response.getName());
	System.err.println(response.getAge());
	System.err.println(response.getSalary());
	System.err.println("**************");
    }

    public void testAnotherBean() throws Exception {

	DirectoryCategory ds = new DirectoryCategory();
	ds.setProperty("fullViewableName", "GetWithIt");
	ds.setProperty("specialEncoding", "JesusTalk");
	
	Envelope requestEnvelope = new Envelope();
	requestEnvelope.setSchema(Constants.SOAP_SCHEMA);
	requestEnvelope.setSchemaInstance(Constants.SOAP_SCHEMA_INSTANCE);
	theCall = new Call(requestEnvelope);
	
	TypeMappingRegistry tmr = new TypeMappingRegistry();
	tmr.mapTypes("http://www.wingfoot.com/xsd",
		     "DirectoryCategory",
		     Class.forName("com.wingfoot.unittestB.DirectoryCategory"),
		     Class.forName("com.wingfoot.soap.encoding.BeanSerializer"),
		     Class.forName("com.wingfoot.soap.encoding.BeanSerializer"));
		     
	theCall.setMappingRegistry(tmr);
	theCall.addParameter("echoDirectorySearch", ds, Class.forName("com.wingfoot.unittestB.DirectoryCategory"));
	//theCall.addParameter("echoDirectorySearch", null, Class.forName("com.wingfoot.unittestB.DirectoryCategory"));
	theCall.setMethodName("echoDirectorySearch");
	theCall.setTargetObjectURI(TARGETURI);

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	DirectoryCategory response = (DirectoryCategory) responseEnvelope.getParameter(0);
	System.err.println("**************");
	System.err.print("The propertyCount is: " + response.getPropertyCount());
	for(int i = 0; i < response.getPropertyCount(); i++){
	    System.err.println("The propertyValue is: " + response.getPropertyValue(i));
	}
	System.err.println("**************");
    }

} /* TestClient */
