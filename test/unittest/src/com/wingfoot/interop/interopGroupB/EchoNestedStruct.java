/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
*/


package com.wingfoot.interop.interopGroupB;
import java.util.*;
import com.wingfoot.soap.encoding.*;
import com.wingfoot.soap.*;
import com.wingfoot.soap.transport.*;

public class EchoNestedStruct  implements InteropInterface
{
	TypeMappingRegistry registry=null;
        J2SEHTTPTransport transport=null;
	String schema, schemaInstance;
	Vector elementMap;
	Vector typeMap;

        public EchoNestedStruct()
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

	    EmployeeBeanBean ebb = new EmployeeBeanBean();
	    ebb.setName("BeanBean");
	    ebb.setAge(new Integer(12));
	    ebb.setSalary(new Float("345.67"));
            ebb.setBean(eb);
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
           registry=new TypeMappingRegistry();
	   registry.mapTypes("http://soapinterop.org/xsd",
	                     "SOAPStruct",
			     Class.forName("com.wingfoot.interop.interopGroupB.EmployeeBean"),
			     Class.forName("com.wingfoot.soap.encoding.BeanSerializer"),
			     Class.forName("com.wingfoot.soap.encoding.BeanSerializer"));
	   registry.mapTypes("http://soapinterop.org/xsd",
	                     "SOAPStructStruct",
			     Class.forName("com.wingfoot.interop.interopGroupB.EmployeeBeanBean"),
			     Class.forName("com.wingfoot.soap.encoding.BeanSerializer"),
			     Class.forName("com.wingfoot.soap.encoding.BeanSerializer"));
            Call call = new Call(requestEnvelope);
	    call.setMappingRegistry(registry);
	    call.addParameter("inputStruct", ebb, Class.forName("com.wingfoot.interop.interopGroupB.EmployeeBeanBean"));
	    call.setMethodName("echoNestedStruct");
	    call.setTargetObjectURI("http://soapinterop.org/");
	    Envelope responseEnvelope = call.invoke(transport);

   	    if (responseEnvelope == null)
	       throw new Exception ("Response envelope is null");
            else if (responseEnvelope.isFaultGenerated()) {
                  Fault f = responseEnvelope.getFault();
		  return "Fault: " + f.getFaultString();
	    }
            else {
                        WSerializable returnString = 
			     (WSerializable) responseEnvelope.getParameter(0);
                        String theName=returnString.getPropertyValue(0)+"";
	         	String theAge = returnString.getPropertyValue(1)+"";
			String theSalary = returnString.getPropertyValue(2)+"";
                        if ( ! (theName.equals("BeanBean") &&
			        theAge.equals("12") &&
				 theSalary.toString().startsWith("345"))) {
                                 throw new Exception("Incorrect return data");
                        }  

			WSerializable returnEB = (WSerializable)
                               returnString.getPropertyValue(3);
			 
                        String theName1=returnEB.getPropertyValue(0)+"";
	         	String theAge1 = returnEB.getPropertyValue(1)+"";
			String theSalary1 = returnEB.getPropertyValue(2)+"";

                        if ( ! (theName1.equals("Tiger Woods") &&
			        theAge1.equals("25") &&
				 theSalary1.startsWith("123"))) {
                                 throw new Exception("Incorrect return data");
                        }  
                   }
              return "OK";
	} //run

} //class
