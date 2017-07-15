package com.wingfoot.interop;

import java.util.*;
import com.wingfoot.soap.*;
import com.wingfoot.soap.encoding.*;
import com.wingfoot.soap.transport.*;
import com.wingfoot.interop.interop.*;
import java.io.*;

/**
 * Retrieves all the endpoints from Whitemeaa resistry.
 * Uses the endpoints to run the interop - Round II.
 */

public class RunInteropB {

    FileOutputStream fos=null;
    String[] service = new String[] { 
                                     "EchoStructAsSimpleTypes",
                                     "EchoSimpleTypesAsStruct",
                                     "EchoNestedStruct",
                                     "EchoNestedArray"
                                   };

    J2SEHTTPTransport transport = new J2SEHTTPTransport("http://www.whitemesa.net/interopInfo","\"http://soapinterop.org/info/\"");

    public static void main(String[] args) throws Exception {

	try{
	    System.err.println("***The groupName is: " + args[0]);
	    new RunInteropB().init(args[0]);
	} catch (Exception e){
	    e.printStackTrace();
	    System.err.println("there was an error: " + e.getMessage());
	}

    }

    public void init(String groupName) throws Exception {

       /**
        * The first thing to do is to open an XML
	* file to store the results.
	*/
         fos = new FileOutputStream ("interopGroupB.html");
         fos.write("<html><head><title>Wingfoot SOAP Interop Results</title></head>\n".getBytes());
         fos.write("<body>\n".getBytes());
         Date d = new Date();
         fos.write(("<h1>Wingfoot Interop Test</h1><br>").getBytes());
         fos.write(("<h3>Results of Round 2 "+ groupName + " test suite as defined in http://www.whitemesa.com/interop.htm</h3><br>").getBytes());
         fos.write(("Interop Test conducted on :" + d + "<br>").getBytes());

	
	Call call = new Call();

	call.addParameter("groupName", groupName, Class.forName("java.lang.String"));
	call.setMethodName("GetEndpointInfo");
	call.setTargetObjectURI("http://soapinterop.org/info/");
	Envelope responseEnvelope = call.invoke(transport);
if (responseEnvelope==null) System.err.println("***ResponseEnvelope is null");
        UntypedObject uo = (UntypedObject)responseEnvelope.getParameter(0);

	for (int i=0; i< uo.getPropertyCount(); i++) {
	     UntypedObject innerUO=(UntypedObject) uo.getPropertyValue(i);
             
             /**
	      * Trap any non-responding servers here.
	      */
	      if (((String)innerUO.getPropertyValue(0)).startsWith("SIM"))
	      //if (!((String)innerUO.getPropertyValue(0)).endsWith("v2.0"))
	          continue;
	     /**
	      * Print out the server name.
	      */
             fos.write(("<h2>SOAP Server: " + innerUO.getPropertyValue(0) +
	   	          "</h2><br>\n").getBytes()); 
	     /**
	      * 0 - Server Name.
	      * 1 - Endpoint.
	      * 2 - WSDL URL.
  	      * For each endpoint, execute the methods
	      * as defined in the service array above.
	      */

	      for (int j=0; j<service.length; j++) {

	      try {
                   Class theClass = Class.forName
		     ("com.wingfoot.interop.interopGroupB."+service[j]);
                    com.wingfoot.interop.interopGroupB.InteropInterface interop = 
                       (com.wingfoot.interop.interopGroupB.InteropInterface)
                                    theClass.newInstance();
                    interop.setTransport(innerUO.getPropertyValue(1)+"");
                    String returnData=interop.run();
		    if (returnData.equals("OK")) 
	                 fos.write((service[j]+": OK <br>\n").getBytes());
                    else
	                 fos.write((service[j]+ ": Fail " + 
			    returnData + "<br>\n").getBytes());
              } catch (Exception e ) {
                   e.printStackTrace();
	           fos.write((service[j]+": Fail:" + 
		        e.getMessage()+"<br>\n").getBytes());
	      }
	      } //j
	     fos.write("<hr>\n".getBytes());
	} //i

    } //init
}
