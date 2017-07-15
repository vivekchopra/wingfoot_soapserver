/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
*/


package com.wingfoot.interop.interopGroupB;
import java.util.*;
import com.wingfoot.soap.encoding.*;
import com.wingfoot.soap.*;
import com.wingfoot.soap.transport.*;

public class EchoStructAsSimpleTypes  implements InteropInterface
{
	TypeMappingRegistry registry=null;
        J2SEHTTPTransport transport=null;
	String schema, schemaInstance;
	Vector elementMap;
	Vector typeMap;

        public EchoStructAsSimpleTypes()
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
	    EmployeeBean eb = new EmployeeBean();
	    eb.setName("Tiger Woods");
	    eb.setAge(new Integer(25));
	    eb.setSalary(new Float("123.45"));
	    registry = new TypeMappingRegistry();
	    registry.mapTypes("http://soapinterop.org/xsd",
	                      "SOAPStruct",
			      Class.forName("com.wingfoot.interop.interopGroupB.EmployeeBean"),
			      Class.forName("com.wingfoot.soap.encoding.BeanSerializer"),
			      Class.forName("com.wingfoot.soap.encoding.BeanSerializer"));
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
	   // requestEnvelope.setSchema(schema);
	   // requestEnvelope.setSchemaInstance(schemaInstance);
            Call call = new Call(requestEnvelope);
	    call.setMappingRegistry(registry);
	    call.addParameter("inputStruct", eb, Class.forName("com.wingfoot.interop.interopGroupB.EmployeeBean"));
	    call.setMethodName("echoStructAsSimpleTypes");
	    call.setTargetObjectURI("http://soapinterop.org/");
	    Envelope responseEnvelope = call.invoke(transport);

   	    if (responseEnvelope == null)
	       throw new Exception ("Response envelope is null");
            else if (responseEnvelope.isFaultGenerated()) {
                  Fault f = responseEnvelope.getFault();
		  return "Fault: " + f.getFaultString();
	    }
            else {
                        String theName= responseEnvelope.getParameter(0)+"";
	         	String theAge = responseEnvelope.getParameter(1)+"";
			String theSalary = responseEnvelope.getParameter(2)+"";

                        if ( ! (theName.equals("Tiger Woods") &&
			        theAge.equals("25") &&
				 theSalary.startsWith("123"))) {
                                 throw new Exception("Incorrect return data");
                        }  
                   }
              return "OK";
	} //run

} //class
