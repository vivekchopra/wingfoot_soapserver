/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
*/


package com.wingfoot.interop.interopGroupB;
import java.util.*;
import java.io.*;
import org.kxml.parser.*;
import org.kxml.*;

public class InteropClient {
    String schema=null;
    String url=null;
    String methodName=null;
    String serverName=null;
    Vector elementMap=null;
    Vector typeMap=null;
    FileOutputStream fos = null;

    public static void main (String argv[])
         throws Exception {
        
	if (argv.length != 1) {
            System.err.println("**Usage: java Interop <name of the xml file> ");
	    return;
	}

	new InteropClient().init(argv[0]);
    } /* main */

    public void init (String fileName)
         throws Exception {

         fos = new FileOutputStream
                   ("interopGroupB.html");
         fos.write("<html><head><title>Wingfoot SOAP Interop Results</title></head>\n".getBytes());
	 fos.write("<body>\n".getBytes());
	 Date d = new Date();
	 fos.write(("<h1>Wingfoot Interop Test</h1><br>").getBytes());
	 fos.write(("<h3>Results of Round 2 Group B Test Suite as defined in http://www.whitemesa.com/interop.htm</h3><br>").getBytes());
	 fos.write(("Interop Test conducted on :" + d + "<br>").getBytes());
	 XmlParser parser = new XmlParser (new InputStreamReader(
	                                   new FileInputStream(fileName)));
         runInterop(parser);
    }
    public void runInterop(XmlParser parser)
          throws Exception {

         while (parser.peek().getType() != Xml.END_DOCUMENT) {
              
             ParseEvent event = parser.read();
	     if (event.getType() == Xml.START_TAG) {
                  
		  if (event.getName().trim().equals("schema")) 
		       storeSchema(parser);
		  else if (event.getName().trim().equals("url"))
		       storeURL(parser);
		  else if (event.getName().trim().equals("service"))
		       storeService(parser);
		  else if (event.getName().trim().equals("servername")) {
		       reinitializeServer();
		       this.serverName=event.getAttribute("name").getValue();
                       fos.write(("<h2>SOAP Server: "
                                  + serverName +"</h2><br>\n"
                                  ).getBytes());
                  }
                  
	     } //if START_TAG
	}//while
	if (fos != null) {
	    fos.write("</body></html>\n".getBytes());
            fos.close();
        }
    } /* runInterop */

    public void reinitializeServer()
         throws Exception {
        this.schema=null;
	this.url=null;
	this.serverName=null;
	reinitializeService();
        fos.write("<hr>\n".getBytes());
    }

    public void storeSchema(XmlParser parser)
         throws Exception {
	while (parser.peek().getType() != Xml.END_DOCUMENT) {
             ParseEvent event = parser.read();
	     if (event.getType() == Xml.END_TAG && 
	         event.getName().trim().equals("schema"))
		 break;
             else if (event.getType() == Xml.TEXT)
	         this.schema=event.getText().trim();
	} //while
        if (this.schema==null)
	     throw new Exception("Error: No Schema specified ");
    } /* storeSchema */
                  
    public void storeURL(XmlParser parser)
        throws Exception {
	while (parser.peek().getType() != Xml.END_DOCUMENT) {
             ParseEvent event = parser.read();
	     if (event.getType() == Xml.END_TAG && 
	         event.getName().trim().equals("url"))
		 break;
             else if (event.getType() == Xml.TEXT)
	         this.url=event.getText().trim();
	} //while
        if (this.url==null)
	     throw new Exception("Error: No URL specified ");
    } /* storeURL */
    
    public void storeService(XmlParser parser) 
        throws Exception {
	/**
	 * A new service; reinitialize service
	 */
	 reinitializeService();
        while (parser.peek().getType() != Xml.END_DOCUMENT) {
             ParseEvent event = parser.read();
             if (event.getType() == Xml.END_TAG &&
	         event.getName().trim().equals("service"))
		 break;
             else if (event.getType() == Xml.START_TAG &&
	              event.getName().trim().equals("name"))
                storeMethodName(parser);
             else if (event.getType() == Xml.START_TAG &&
	              event.getName().trim().equals("elementmap"))
                storeElementMap(parser);
             else if (event.getType() == Xml.START_TAG &&
	              event.getName().trim().equals("typemap"))
                storeTypeMap(parser);
	} //while
        /**
         * We have all the data to run the interop test for
	 * the methodName.  Use J2SE reflection to invoke
	 * the proper service
	 */
	 Class theClass = Class.forName("com.wingfoot.interop.interopGroupB."+methodName);
	 InteropInterface interop = (InteropInterface)
	                            theClass.newInstance();
         if (this.schema.equals("1999")) {
             interop.setSchema("http://www.w3.org/1999/XMLSchema");
             interop.setSchemaInstance
	            ("http://www.w3.org/1999/XMLSchema-instance");
	 }
	 else {
             interop.setSchema("http://www.w3.org/2001/XMLSchema");
             interop.setSchemaInstance
	            ("http://www.w3.org/2001/XMLSchema-instance");
	 }
         interop.setTransport(url);
	 interop.setElementMap(elementMap);
	 interop.setTypeMap(typeMap);
         try {
	         String returnData = interop.run();
	         if (returnData.equals("OK"))
	             fos.write((methodName+": OK <br>\n").getBytes());
                 else
                     fos.write((methodName+
                               ": Fail " + 
                                returnData + "<br>\n").getBytes());
             } catch (Exception e) {
                     e.printStackTrace();
                     fos.write((methodName+": Fail:" +
					e.getMessage()+"<br>\n").getBytes());
	       }
    } /* storeService(XmlParser) */

    public void reinitializeService() {
         this.methodName=null;
	 this.elementMap=null;
	 this.typeMap=null;
    }

    public void storeMethodName(XmlParser parser)
         throws Exception {
        
	while (parser.peek().getType() != Xml.END_DOCUMENT) {
             ParseEvent event = parser.read();
             if (event.getType() == Xml.END_TAG &&
	         event.getName().trim().equals("name"))
		 break;
             else if (event.getType() == Xml.TEXT)
	         this.methodName=event.getText().trim();
        }
        if (this.methodName==null)
	     throw new Exception("Error: No <name> specified ");
    } /* storeMethodName */

    public void storeElementMap(XmlParser parser)
            throws Exception {
        String lastElement=null;
	String elementNameToMap=null;
	String elementClass=null;
	String deserializerClass=null;
        while (parser.peek().getType() != Xml.END_DOCUMENT) {
             ParseEvent event = parser.read();
             if (event.getType() == Xml.END_TAG &&
	         event.getName().trim().equals("elementmap"))
		 break;
             else if (event.getType()==Xml.START_TAG)
                 lastElement=event.getName().trim();
	     else if (event.getType()==Xml.TEXT) {
                 if (lastElement.equals("elementname"))
		     elementNameToMap=event.getText().trim();
                 else if (lastElement.equals("elementclass"))
		     elementClass=event.getText().trim();
                 else if (lastElement.equals("deserializerclass"))
		     deserializerClass=event.getText().trim();
	     }
        } //while
	if (elementMap==null) elementMap=new Vector();
        if (deserializerClass.equals("null")) 
             elementMap.addElement(new String[] { elementNameToMap,
	                                          elementClass,
		          			  null
				                 });
        else
             elementMap.addElement(new String[] { elementNameToMap,
	                                          elementClass,
		          			  deserializerClass
				                });
    } /* storeElementMap*/

    public void storeTypeMap(XmlParser parser) 
            throws Exception {
        String lastElement=null;
	String namespace=null;
	String localpart=null;
	String objectclass=null;
	String serializer=null;
	String deserializer=null;

        while (parser.peek().getType() != Xml.END_DOCUMENT) {
             ParseEvent event = parser.read();
             if (event.getType() == Xml.END_TAG &&
	         event.getName().trim().equals("typemap"))
		 break;
             else if (event.getType()==Xml.START_TAG)
                 lastElement=event.getName().trim();
	     else if (event.getType()==Xml.TEXT) {
                 if (lastElement.equals("namespace"))
		     namespace=event.getText().trim();
                 else if (lastElement.equals("localpart"))
		     localpart=event.getText().trim();
                 else if (lastElement.equals("objectclass"))
		     objectclass=event.getText().trim();
                 else if (lastElement.equals("serializer"))
		     serializer=event.getText().trim();
                 else if (lastElement.equals("deserializer"))
		     deserializer=event.getText().trim();
	     }
        } //while
	if (typeMap==null) typeMap=new Vector();
        typeMap.addElement(new String[] { namespace,
	                                     localpart,
					     objectclass,
					     serializer,
					     deserializer
				           });

    } /* storeTypeMap*/

} /* class InteropClient */



