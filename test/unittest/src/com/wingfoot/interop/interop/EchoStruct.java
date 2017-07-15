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

public class EchoStruct  implements InteropInterface
{
	TypeMappingRegistry registry=null;
        J2SEHTTPTransport transport=null;
	String schema, schemaInstance;
	Vector elementMap;
	Vector typeMap;

        public EchoStruct()
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
            registry = new TypeMappingRegistry();
	    registry.mapTypes("http://soapinterop.org/xsd",
	                      "SOAPStruct",
			      Class.forName("com.wingfoot.interop.interop.EmployeeBean"),
			      Class.forName("com.wingfoot.soap.encoding.BeanSerializer"),
			      Class.forName("com.wingfoot.soap.encoding.BeanSerializer"));
	    registry.mapElements("Result",
	          Class.forName("com.wingfoot.interop.interop.EmployeeBean"),
		  Class.forName("com.wingfoot.soap.encoding.BeanSerializer"));


	    Envelope requestEnvelope = new Envelope();
	    //requestEnvelope.setSchema(schema);
	    //requestEnvelope.setSchemaInstance(schemaInstance);
            Call call = new Call(requestEnvelope);
	    call.setMappingRegistry(registry);
	    call.addParameter("inputStruct", eb, Class.forName("com.wingfoot.interop.interop.EmployeeBean"));
	    call.setMethodName("echoStruct");
	    call.setTargetObjectURI("http://soapinterop.org/");
	    Envelope responseEnvelope = call.invoke(transport);

   	    if (responseEnvelope == null)
	       throw new Exception ("Response envelope is null");
            else if (responseEnvelope.isFaultGenerated()) {
                  Fault f = responseEnvelope.getFault();
		  return "Fault: " + f.getFaultString();
	    }
            else {
	               String theName=null;
		       Integer theAge=null;
                       Float theSalary=null;
	               if (responseEnvelope.getParameter(0) instanceof UntypedObject) {
                            UntypedObject uo = (UntypedObject)
			                       responseEnvelope.getParameter(0);
                            theName=(String)uo.getPropertyValue(0);

			    if (uo.getPropertyValue(1) instanceof String) 
			         theAge = new Integer((String)uo.getPropertyValue(1));
                            else
			         theAge = (Integer)uo.getPropertyValue(1);

			    if (uo.getPropertyValue(2) instanceof String) 
			         theSalary = new Float((String)uo.getPropertyValue(2));
                            else
			         theSalary = (Float)uo.getPropertyValue(2);
		       } //if
		       else {
                              EmployeeBean returnString = 
			          (EmployeeBean) responseEnvelope.getParameter(0);
                              theName=returnString.getName();
	         	      theAge = returnString.getAge();
			      theSalary = returnString.getSalary();
                       }//else

                        if ( ! (theName.equals("Tiger Woods") &&
			        theAge.intValue() == 25 &&
				 theSalary.toString().startsWith("123"))) {
                                 throw new Exception("Incorrect return data");
                        }  
                   } //else
              return "OK";
	} //run

} //class
