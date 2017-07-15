/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
*/


package com.wingfoot.interop.interop;
import java.util.*;
import java.lang.*;
import com.wingfoot.soap.encoding.*;
import com.wingfoot.soap.*;
import com.wingfoot.soap.transport.*;

public class EchoStructArray  implements InteropInterface
{
	TypeMappingRegistry registry=null;
        J2SEHTTPTransport transport=null;
	String schema, schemaInstance;
	Vector elementMap;
	Vector typeMap;

        public EchoStructArray()
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

	    EmployeeBean eb1 = new EmployeeBean();
	    eb1.setName("Vijay Singh");
	    eb1.setAge(new Integer(37));
	    eb1.setSalary(new Float("67.89"));

            EmployeeBean[] ebArray = new EmployeeBean[] {eb, eb1};
    /**
	    if (elementMap !=null ||
	        typeMap != null) {
		registry = new TypeMappingRegistry();
		for (int i=0; elementMap!=null && i<elementMap.size(); i++) {
		    String[] s = (String[]) elementMap.elementAt(i);
                    if (s[1].trim().equals("array")) {
                          s[1]=new EmployeeBean[1].getClass().getName();
                    }

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
            registry = new TypeMappingRegistry();
            registry.mapTypes("http://soapinterop.org/xsd",
                              "SOAPStruct",
                              Class.forName("com.wingfoot.interop.interop.EmployeeBean"),
                              Class.forName("com.wingfoot.soap.encoding.BeanSerializer"),
                              Class.forName("com.wingfoot.soap.encoding.BeanSerializer"));
	    
	    Envelope  requestEnvelope = new Envelope();
	   // requestEnvelope.setSchema(schema);
	   // requestEnvelope.setSchemaInstance(schemaInstance);
            Call call = new Call(requestEnvelope);
	    call.setMappingRegistry(registry);
	    call.addParameter("inputStructArray", ebArray, Class.forName((new com.wingfoot.interop.interop.EmployeeBean[1]).getClass().getName()));
	    call.setMethodName("echoStructArray");
	    call.setTargetObjectURI("http://soapinterop.org/");
	    Envelope responseEnvelope = call.invoke(transport);

   	    if (responseEnvelope == null)
	       throw new Exception ("Response envelope is null");
            else if (responseEnvelope.isFaultGenerated()) {
                  Fault f = responseEnvelope.getFault();
		  return "Fault: " + f.getFaultString();
	    }
            else {
	              WSerializable ws1=null;
	              WSerializable ws2=null;

	              if (responseEnvelope.getParameter(0) instanceof UntypedObject) {
                           UntypedObject uo = (UntypedObject)
			                      responseEnvelope.getParameter(0);
                           ws1=(WSerializable) uo.getPropertyValue(0);
                           ws2=(WSerializable) uo.getPropertyValue(1);
		      }
		      else {
		           Object[] employeeArray = (Object[])
                               responseEnvelope.getParameter(0);
                           ws1 = (WSerializable) employeeArray[0];
                           ws2 = (WSerializable) employeeArray[1];
                      }
                      String theName1=null;
                      String theName2=null;
		      Integer theAge1=null;
		      Integer theAge2=null;
		      Float theSalary1=null;
		      Float theSalary2=null;

		      for (int i=0; i<ws1.getPropertyCount(); i++) {
                           if (ws1.getPropertyName(i).equals("varString"))
			        theName1=(String)ws1.getPropertyValue(i);
                           else if (ws1.getPropertyName(i).equals("varInt")) {
                                if (ws1.getPropertyValue(i) instanceof Integer)
			            theAge1=(Integer)ws1.getPropertyValue(i);
                                else
				    theAge1= new Integer((String)ws1.getPropertyValue(i));
                           }
                           else if (ws1.getPropertyName(i).equals("varFloat")) {
                                if (ws1.getPropertyValue(i) instanceof Float)
			            theSalary1=(Float)ws1.getPropertyValue(i); 
                                else
				    theSalary1= 
				    new Float((String)ws1.getPropertyValue(i));
                           }
		      }
		      for (int i=0; i<ws2.getPropertyCount(); i++) {
                           if (ws2.getPropertyName(i).equals("varString"))
			        theName2=(String)ws2.getPropertyValue(i);
                           else if (ws2.getPropertyName(i).equals("varInt")) {
                                if (ws2.getPropertyValue(i) instanceof Integer)
			             theAge2=(Integer)ws2.getPropertyValue(i);
                                else
				    theAge2= new Integer((String)ws2.getPropertyValue(i));
                           }
                           else if (ws2.getPropertyName(i).equals("varFloat")) {
                                if (ws2.getPropertyValue(i) instanceof Float)
			           theSalary2=(Float)ws2.getPropertyValue(i);
                                else
				    theSalary2= 
				    new Float((String)ws2.getPropertyValue(i));
                           }
		      }

                       if ( ! (theName1.equals("Tiger Woods") &&
		               theAge1.intValue() == 25 &&
		               theSalary1.toString().startsWith("123"))) {
                             throw new Exception("Incorrect return data");
                        }
                       if (!(theName2.equals("Vijay Singh") &&
		             theAge2.intValue() == 37 &&
		             theSalary2.toString().startsWith("67"))) {
                           throw new Exception("Incorrect return data");
                        }
                   }
              return "OK";
	} //run

} //class
