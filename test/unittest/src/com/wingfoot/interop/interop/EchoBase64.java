/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */


package com.wingfoot.interop.interop;
import java.util.*;
import com.wingfoot.soap.encoding.*;
import com.wingfoot.soap.*;
import com.wingfoot.soap.transport.*;

public class EchoBase64  implements InteropInterface
{
    TypeMappingRegistry registry=null;
    J2SEHTTPTransport transport=null;
    String schema, schemaInstance;
    Vector elementMap;
    Vector typeMap;

    public EchoBase64()
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
	Base64 b64 = new Base64("Hello World".getBytes());
  /**
	if (elementMap !=null ||
	    typeMap != null) {
	    registry = new TypeMappingRegistry();
	    for (int i=0; elementMap!=null && i<elementMap.size(); i++) {
		String[] s = (String[]) elementMap.elementAt(i);
		if (s[2]==null) {
		    registry.mapElements(s[0],
					 Class.forName(s[1]),
					 null);
		}
		else {
		    registry.mapElements(s[0],
					 Class.forName(s[1]),
					 Class.forName(s[2]));
		}
	    }

	    for (int i=0; typeMap!=null && i<typeMap.size(); i++) {
		String[] s = (String[]) typeMap.elementAt(i);
		registry.mapTypes(s[0],
				  s[1],
				  Class.forName(s[2]),
				  Class.forName(s[3]), 
				  Class.forName(s[4]));
	    }
	}
  **/
	Envelope requestEnvelope = new Envelope();
	//requestEnvelope.setSchema(schema);
	//requestEnvelope.setSchemaInstance(schemaInstance);
	Call call = new Call(requestEnvelope);
	call.setMappingRegistry(registry);
	call.addParameter("inputBase64", b64, Class.forName("com.wingfoot.soap.encoding.Base64"));
	//call.addParameter("inputBase64", b64, tempByte.getClass());
	call.setMethodName("echoBase64");
	call.setTargetObjectURI("http://soapinterop.org/");
	Envelope responseEnvelope = call.invoke(transport);

	if (responseEnvelope == null)
	    throw new Exception ("Response envelope is null");
	else if (responseEnvelope.isFaultGenerated()) {
	    Fault f = responseEnvelope.getFault();
	    return "Fault: " + f.getFaultString();
	}
	else {
	    byte[] bb=null;
            Base64 returnString=null;
            if (responseEnvelope.getParameter(0) instanceof String) {
                //Base64 br64 = new Base64((String)responseEnvelope.getParameter(0));
                returnString = new Base64((String)responseEnvelope.getParameter(0));
		//bb=br64.getBytes();
            }
	    else {
	        returnString = (Base64) responseEnvelope.getParameter(0);
	      //  bb = (byte[]) responseEnvelope.getParameter(0);
            }
	    bb = returnString.getBytes();
	    String newString = new String(bb);
	    if ( ! newString.equals("Hello World"))
		throw new Exception("Incorrect return data");
                   
	}
	return "OK";
    } //run

} //class
