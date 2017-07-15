
package com.wingfoot.unittest;

import com.wingfoot.soap.*;
import com.wingfoot.soap.encoding.*;
import com.wingfoot.soap.transport.*;
import java.util.*;

public class TestClient {
     
    private String theString;
    private Call theCall;
    private J2SEHTTPTransport  theTransport;
     
    public TestClient() throws Exception {
	theTransport = new J2SEHTTPTransport(
	     "http://localhost:8080/wingfoot/servlet/wserver", "");
        theTransport.addHeader("Kal", "Iyer");
    } 

    public static void main (String argv[])  {
	try {
	    TestClient tc = new TestClient();
	    tc.testString("Hello World");
	    //tc.testUntypedString();
	      
	    //   tc.testByte(new Byte((byte)1));
	    //  tc.testShort(new Short((short)100));
	    //   tc.testInteger(new Integer(1000));
	     //  tc.testInteger(null);
	    //  tc.testLong(new Long((long)10000000));
	    //  tc.testFloat(new Float((float)10.1234));
	    //  tc.testDouble(new Double((double)10.12344567890));
	    //  tc.testInteger(new Integer(Integer.MAX_VALUE));
	    //    tc.testInteger(new Integer(Integer.MIN_VALUE));
	    //    tc.testFloat(new Float(Float.MAX_VALUE));
	    //    tc.testFloat(new Float(Float.MIN_VALUE));
	    //    tc.testFloat(new Float(Float.NaN));
	    //    tc.testFloat(new Float(Float.NEGATIVE_INFINITY));
	    //   tc.testFloat(new Float(Float.POSITIVE_INFINITY));
	    //    tc.testDouble(new Double(Double.MAX_VALUE));
	    //    tc.testDouble(new Double(Double.MIN_VALUE));
	    //    tc.testDouble(new Double(Double.NaN));
	    //     tc.testDouble(new Double(Double.NEGATIVE_INFINITY));
	    //     tc.testDouble(new Double(Double.POSITIVE_INFINITY));
	    //    tc.testDate(new Date());
	    //    tc.testBoolean(new Boolean(true));
	    //    tc.testBoolean(new Boolean(false));
	    //   tc.testBase64(new Base64("Hello World".getBytes()));
	    //   tc.testHexBinary(new HexBinary("Hello World Hex".getBytes()));
	    //  tc.testString(new String());
	    //   tc.testString("&%^*");
	    //   tc.testString("Hello 		World");
	    //  tc.testString("Hello 		World");
	      // tc.testString(null)
	    //   tc.testString(";\"'");
	    //tc.testInteropPoints();
    	    //tc.testInteropPointsB();
	    
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

    public void testInteropPoints() throws Exception {
	theCall = new Call();
	theCall.addParameter("testInterop", "/home/baldwinl/devel/wingfoot_development/soapserver/unittest/src/com/wingfoot/interop/interop/interop.xml", Class.forName("java.lang.String"));
	theCall.setMethodName("testInterop");
	theCall.setTargetObjectURI("urn:testinterop");
	Envelope responseEnvelope = theCall.invoke(theTransport);
	printResponseInformation(responseEnvelope);
    }

    public void testInteropPointsB() throws Exception {
	theCall = new Call();
	theCall.addParameter("testInteropB", "/home/baldwinl/devel/wingfoot_development/soapserver/unittest/src/com/wingfoot/interop/interopGroupB/interopGroupB.xml", Class.forName("java.lang.String"));
	theCall.setMethodName("testInteropB");
	theCall.setTargetObjectURI("urn:testinteropB");
	Envelope responseEnvelope = theCall.invoke(theTransport);
	printResponseInformation(responseEnvelope);
    }
    
    public void testString(String str) throws Exception {
         
	theCall = new Call();
	theCall.addParameter("echoString", str, Class.forName("java.lang.String"));
	//theCall.addParameter("echoString", "baldwin");
	theCall.setMethodName("echoString");
	theCall.setTargetObjectURI("urn:echo-string");

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	String response = (String) responseEnvelope.getParameter(0);
	System.err.println(response);
    }

    public void testUntypedString() throws Exception {

	Envelope requestEnvelope = new Envelope();
	String xmlString = "<ns1:echoString xmlns:ns1='urn:echo-string'> " +
	                   "<echoString>Hello World Let This Work</echoString> " +
 	                   "</ns1:echoString> ";

	requestEnvelope.setBody(xmlString);
	theCall = new Call(requestEnvelope);
	Envelope responseEnvelope = theCall.invoke(theTransport);
	System.err.println("Just returned from the server");
	System.err.println("The result is: " + responseEnvelope.getParameter(0));

    }

    public void testByte(Byte theByte) throws Exception {

	theCall = new Call();
	theCall.addParameter("echoByte", theByte, Class.forName("java.lang.Byte"));
	theCall.setMethodName("echoByte");
	theCall.setTargetObjectURI("urn:echo-byte");

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Byte response = (Byte) responseEnvelope.getParameter(0);
	System.err.println(response);
    }

    public void testShort(Short str) throws Exception {

	theCall = new Call();
	theCall.addParameter("echoShort", str, Class.forName("java.lang.Short"));
	theCall.setMethodName("echoShort");
	theCall.setTargetObjectURI("urn:echo-short");

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Short response = (Short) responseEnvelope.getParameter(0);
	System.err.println(response);
    }

    public void testInteger(Integer str) throws Exception {

	theCall = new Call();
	theCall.addParameter("echoInteger", str, Class.forName("java.lang.Integer"));
	theCall.setMethodName("echoInt");
	theCall.setTargetObjectURI("urn:echo-int");

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Integer response = (Integer) responseEnvelope.getParameter(0);
	System.err.println(response);
    }

    public void testLong(Long str) throws Exception {

	theCall = new Call();
	theCall.addParameter("echoLong", str, Class.forName("java.lang.Long"));
	theCall.setMethodName("echoLong");
	theCall.setTargetObjectURI("urn:echo-long");

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Long response = (Long) responseEnvelope.getParameter(0);
	System.err.println(response);
    }

    public void testFloat(Float str) throws Exception {

	theCall = new Call();
	theCall.addParameter("echoFloat", str, Class.forName("java.lang.Integer"));
	theCall.setMethodName("echoFloat");
	theCall.setTargetObjectURI("urn:echo-float");

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Float response = (Float) responseEnvelope.getParameter(0);
	System.err.println(response);
    }

    public void testDouble(Double str) throws Exception {

	theCall = new Call();
	theCall.addParameter("echoDouble", str, Class.forName("java.lang.Double"));
	theCall.setMethodName("echoDouble");
	theCall.setTargetObjectURI("urn:echo-double");

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Double response = (Double) responseEnvelope.getParameter(0);
	System.err.println(response);
    }
     
    public void testDate(Date str) throws Exception {

	theCall = new Call();
	theCall.addParameter("echoDate", str, Class.forName("java.util.Date"));
	theCall.setMethodName("echoDate");
	theCall.setTargetObjectURI("urn:echo-date");

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Date response = (Date) responseEnvelope.getParameter(0);
	System.err.println("Original Date:" + str);
	System.err.println(response);
          
	if (str.equals(response)) System.err.println("Dates are equal");
	else System.err.println("Dates are not equal");
    }
     
    public void testBoolean(Boolean str) throws Exception {

	theCall = new Call();
	theCall.addParameter("echoBoolean", str, Class.forName("java.lang.Boolean"));
	theCall.setMethodName("echoBoolean");
	theCall.setTargetObjectURI("urn:echo-boolean");

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Boolean response = (Boolean) responseEnvelope.getParameter(0);
	System.err.println(response);
    }

    public void testBase64(Base64 str) throws Exception {

	theCall = new Call();
	theCall.addParameter("echoBase64", str, Class.forName("com.wingfoot.soap.encoding.Base64"));
	theCall.setMethodName("echoBase64");
	theCall.setTargetObjectURI("urn:echo-base64");

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	Base64 response = (Base64) responseEnvelope.getParameter(0);
	System.err.println(new String(response.getBytes()));
    }
    public void testHexBinary(com.wingfoot.soap.encoding.HexBinary str) throws Exception {
	theCall = new Call();
	theCall.addParameter("echoHexBinary", str, Class.forName("com.wingfoot.soap.encoding.HexBinary"));
	theCall.setMethodName("echoHexBinary");
	theCall.setTargetObjectURI("urn:echo-hexbinary");

	Envelope responseEnvelope = theCall.invoke(theTransport);
          
	printResponseInformation(responseEnvelope);
	com.wingfoot.soap.encoding.HexBinary response = (com.wingfoot.soap.encoding.HexBinary) responseEnvelope.getParameter(0);
	System.err.println(new String(response.getBytes()));
    }
} /* TestClient */
