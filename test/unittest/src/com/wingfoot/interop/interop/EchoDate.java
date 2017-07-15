/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
*/


package com.wingfoot.interop.interop;
import java.util.*;
import com.wingfoot.soap.encoding.*;
import com.wingfoot.soap.*;
import com.wingfoot.soap.transport.*;

public class EchoDate  implements InteropInterface
{
	TypeMappingRegistry registry=null;
        J2SEHTTPTransport transport=null;
	String schema, schemaInstance;
	Vector elementMap;
	Vector typeMap;

        public EchoDate()
	{
	}
        public void setTransport(String url) {

  	     this.transport = new J2SEHTTPTransport (url,
	                                            "\"http://soapinterop.org/\"");
             this.transport.getResponse(true);
        }
	public void setSchema(String schema) {
            this.schema=schema;        
	}
	public void setSchemaInstance(String schemaInstance) {
            this.schemaInstance=schemaInstance;        
	}
        
	public void setElementMap(Vector elementMap) {
            this.elementMap=elementMap;
	} //setElementMap

	public void setTypeMap(Vector typeMap) {
            this.typeMap=typeMap;
	} //setTypeMap

	public String run() throws Exception
	{
	    Envelope requestEnvelope = new Envelope();
	//    requestEnvelope.setSchema(schema);
	 //   requestEnvelope.setSchemaInstance(schemaInstance);
	    Date originalDate=new Date();
            Call call = new Call(requestEnvelope);

		registry = new TypeMappingRegistry();
                registry.mapElements("Return", Class.forName("java.util.Date"), null);
                registry.mapElements("return", Class.forName("java.util.Date"), null);
                registry.mapElements("Result", Class.forName("java.util.Date"), null);
                registry.mapElements("result", Class.forName("java.util.Date"), null);

	    call.setMappingRegistry(registry);
	    call.addParameter("inputDate", originalDate, Class.forName("java.util.Date"));
	    call.setMethodName("echoDate");
	    call.setTargetObjectURI("http://soapinterop.org/");
	    Envelope responseEnvelope = call.invoke(transport);

   	    if (responseEnvelope == null)
	       throw new Exception ("Response envelope is null");
            else if (responseEnvelope.isFaultGenerated()) {
                  Fault f = responseEnvelope.getFault();
		  return "Fault: " + f.getFaultString();
	    }
            else { 
	         Date theReturnArray=null;

                 if (responseEnvelope.getParameter(0) instanceof String) 
                      theReturnArray = new Date((String) responseEnvelope.getParameter(0));
                 else
		       theReturnArray = (Date) responseEnvelope.getParameter(0);
                 if (! originalDate.equals(theReturnArray)) {
		      
		      if (originalDate.getDate()!= theReturnArray.getDate() ||
		          originalDate.getMonth()!= theReturnArray.getMonth() ||
		          originalDate.getYear()!= theReturnArray.getYear() ||
		          originalDate.getHours()!= theReturnArray.getHours() ||
		          originalDate.getMinutes()!= theReturnArray.getMinutes() ||
		          originalDate.getSeconds()!= theReturnArray.getSeconds())

                             throw new Exception("Incorrect return data");
                 }
             }
                   
              return "OK";
	} //run

} //class
