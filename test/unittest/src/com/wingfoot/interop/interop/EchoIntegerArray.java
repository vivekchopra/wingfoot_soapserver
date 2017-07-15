/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
*/


package com.wingfoot.interop.interop;
import java.util.*;
import com.wingfoot.soap.encoding.*;
import com.wingfoot.soap.*;
import com.wingfoot.soap.transport.*;

public class EchoIntegerArray
         implements InteropInterface
{
	TypeMappingRegistry registry=null;
        J2SEHTTPTransport transport=null;
	String schema, schemaInstance;
	Vector elementMap;
	Vector typeMap;

        public EchoIntegerArray()
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
            Integer[] strArray = new Integer[] 
	                      {new Integer(123), new Integer(456)};
   /**
	    if (elementMap !=null ||
	        typeMap != null) {
		registry = new TypeMappingRegistry();
		for (int i=0; elementMap!=null && i<elementMap.size(); i++) {
		    String[] s = (String[]) elementMap.elementAt(i);
		    if (s[1].trim().equals("array")) {
                          s[1]=new Integer[1].getClass().getName();
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
	    Envelope requestEnvelope = new Envelope();
	   // requestEnvelope.setSchema(schema);
	   // requestEnvelope.setSchemaInstance(schemaInstance);
            Call call = new Call(requestEnvelope);
	    call.setMappingRegistry(registry);
	    call.addParameter("inputIntegerArray", strArray, Class.forName((new Integer[1]).getClass().getName()));
	    call.setMethodName("echoIntegerArray");
	    call.setTargetObjectURI("http://soapinterop.org/");
	    Envelope responseEnvelope = call.invoke(transport);

   	    if (responseEnvelope == null)
	       throw new Exception ("Response envelope is null");
            else if (responseEnvelope.isFaultGenerated()) {
	          Fault f = responseEnvelope.getFault();
	          return "Fault: " + f.getFaultString();
	     }
            else {
	              Integer str1=null;
	              Integer str2=null;

		      if (responseEnvelope.getParameter(0) instanceof UntypedObject) {
                           UntypedObject uo = (UntypedObject)
			                     responseEnvelope.getParameter(0);

                           if (uo.getPropertyValue(0) instanceof String)
			        str1=new Integer((String)uo.getPropertyValue(0));
                           else if (uo.getPropertyValue(0) instanceof Integer)
			        str1=(Integer)uo.getPropertyValue(0);
                           
                           if (uo.getPropertyValue(1) instanceof String)
			        str2=new Integer((String)uo.getPropertyValue(1));
                           else if (uo.getPropertyValue(1) instanceof Integer)
			        str2=(Integer)uo.getPropertyValue(1);
		      }
		      else {
                          Object[] returnString = 
		             (Object[]) responseEnvelope.getParameter(0);
		          str1 = (Integer) returnString[0];
		          str2 = (Integer) returnString[1];
                      }
                         // System.err.println(str1 + " " + str2);
		          if ( !(str1.intValue() == 123 &&
		             str2.intValue() ==456))
			       throw new Exception("Incorrect return data");
                   }
              return "OK";
	} //run

} //class
