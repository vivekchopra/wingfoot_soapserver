/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
*/


package com.wingfoot.interop.interopGroupB;
import java.util.*;
import com.wingfoot.soap.encoding.*;
import com.wingfoot.soap.*;
import com.wingfoot.soap.transport.*;

public class EchoNestedArray  implements InteropInterface
{
	TypeMappingRegistry registry=null;
        J2SEHTTPTransport transport=null;
	String schema, schemaInstance;
	Vector elementMap;
	Vector typeMap;

        public EchoNestedArray()
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
	    EmployeeBeanNestedArray eb = new EmployeeBeanNestedArray();
	    eb.setName("Tiger Woods");
	    eb.setAge(25);
	    eb.setSalary(new Float("123.45"));
	    eb.setArray(new String[] {"Red", "Green", "Blue"});
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
	                      "SOAPArrayStruct",
			      Class.forName("com.wingfoot.interop.interopGroupB.EmployeeBeanNestedArray"),
			      Class.forName("com.wingfoot.soap.encoding.BeanSerializer"),
			      Class.forName("com.wingfoot.soap.encoding.BeanSerializer"));

	    Envelope requestEnvelope = new Envelope();
	   // requestEnvelope.setSchema(schema);
	   // requestEnvelope.setSchemaInstance(schemaInstance);
            Call call = new Call(requestEnvelope);
	    call.setMappingRegistry(registry);
	    call.addParameter("inputStruct", eb, Class.forName("com.wingfoot.interop.interopGroupB.EmployeeBeanNestedArray"));
	    call.setMethodName("echoNestedArray");
	    call.setTargetObjectURI("http://soapinterop.org/");
	    Envelope responseEnvelope = call.invoke(transport);

   	    if (responseEnvelope == null)
	       throw new Exception ("Response envelope is null");
            else if (responseEnvelope.isFaultGenerated()) {
                  Fault f = responseEnvelope.getFault();
		  return "Fault: " + f.getFaultString();
	    }
            else {
                       //EmployeeBeanNestedArray
                        WSerializable returnString = 
			     (WSerializable) responseEnvelope.getParameter(0);

                        String name=returnString.getPropertyValue(0)+"";
                        String age=returnString.getPropertyValue(1)+"";
                        String salary=returnString.getPropertyValue(2)+"";

                        if ( ! (name.equals("Tiger Woods") &&
			        age.equals("25") &&
				salary.startsWith("123"))) {
                                 throw new Exception("Incorrect return data");
                        }
                        
			Object strArray = returnString.getPropertyValue(3);

			if (strArray.getClass().isArray()) {
			     Object theArray[] = (Object[]) strArray;
			     if ( !(((String)theArray[0]).equals("Red") &&
			            ((String)theArray[1]).equals("Green") &&
			            ((String)theArray[2]).equals("Blue")))
                                      throw new Exception("Incorrect return data");
			}
			else if (strArray instanceof com.wingfoot.soap.encoding.UntypedObject) {
			     UntypedObject uo = (UntypedObject) strArray;
                             String str1 = (String)uo.getPropertyValue(0);
                             String str2 = (String)uo.getPropertyValue(1);
                             String str3 = (String)uo.getPropertyValue(2);
			     if ( !(str1.equals("Red") &&
			            str2.equals("Green") &&
			            str3.equals("Blue")))
                                      throw new Exception("Incorrect return data");
			}
			else {
                                 throw new Exception("Unknown datatype returned.");
			}

                   }
              return "OK";
	} //run

} //class
