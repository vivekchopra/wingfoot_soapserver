/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */

//package com.wingfoot.soap.rpc;
package com.wingfoot.soap;

import java.io.*;
import java.util.*;
import com.wingfoot.*;
import com.wingfoot.soap.*;
import com.wingfoot.wsdl.*;
import com.wingfoot.wsdl.soap.*;
import com.wingfoot.soap.encoding.*;
import com.wingfoot.soap.transport.*;
import org.kxml.io.*;
import org.kxml.parser.*;

/**
 * Entry point for making a SOAP call.
 * The call could be a RPC style body or
 * Document style body.
 * This class is used to send a SOAP payload
 * to the SOAP server.
 * @since 0.90
 * @author Kal Iyer
 */

public class Call {

  // URI to post the SOAP call
  //private String              uri;
  // The SOAP Envelope to send in the RPC call
  private LiteralEnvelope  envelope;   
    
  // The transport for the SOAP RPC call
  private Transport           transport;

  private TypeMappingRegistry registry;

  //String methodName, targetURI;


    /**
     * Constructor using a custom SOAP Envelope
     * built by user. A custom Envelope is required
     * if 
     * <ul>
     * <li> an alternative schema and schema instance is needed (default 2001); or
     * <li> a SOAP Header is needed; or
     * <li> a Document Style SOAP Body is required.
     * </ul>
     * @since 0.90
     */
  public Call (LiteralEnvelope envelope) {
    this.envelope = envelope;
  } /* constructor */

    /**
     * Sets the URI of the service on the server. 
     * This is typically required only for a RPC
     * style service.
     * @since 0.90
     * @param uri URI for the service on the server.
     */
  //public void setTargetObjectURI (String uri) {
    //this.targetURI=uri;
 // } /* setTargetObjectURI*/

    /**
     * Sets the name of the method to invoke on
     * the server.  This is typically required
     * only for a RPC style service.
     * @since 0.90
     * @param methodName the name of the method to
     * invoke on the service.
     */
  //public void setMethodName (String methodName) {
    //this.methodName=methodName;
  //} /* setMethodName */

    /**
     * Sets the registry to associate a Class with
     * the serializer class, desserializer class and
     * the name of the class.  The registry is useful
     * in serialization and deserialization.
     * This is typically required only for a RPC style
     * service.
     * @since 0.90
     * @param registry instance of TypeMappingRegistry
     */
  public void setMappingRegistry (TypeMappingRegistry registry) {
    this.registry = registry;
  }

  /**
   * Call to serialize and send the soap payload to the
   * server and get back the response. The response is
   * encapsulated in Envelope
   * @since 0.90
   * @param transport an implementation of Transport interface.
   * An instance of HTTPTransport is provided to use HTTP as the
   * transport to communicate to the service on the server. 
   * Users can write their own Transport (example SMTP instead of
   * HTTP).
   * @return the response from the service encapsulated as
   * an Envelope.
   */
  public LiteralEnvelope invoke (Transport transport) 
    throws SOAPException, Exception 
  {
    if (envelope == null)
      throw new SOAPException ("Soap Envelope cannot be null");

    /**
     * Serialize the envelope. This in turn will serialize the
     * header and body along with any attributes.
     */
    byte[] payload=null;
    Hashtable hashtable = new Hashtable();
    hashtable.put (Constants.SOAP_NAMESPACE,"SOAP-ENV");
    hashtable.put (envelope.getSchemaInstance(), "xsi");
    hashtable.put (envelope.getSchema(), "xsd");
    XMLWriter xmlwriter = new XMLWriter(hashtable);
    SOAPSerializer ss = null;
    if (this.isMessageLiteral(envelope))
      ss=new LiteralSerializer((LiteralEnvelope)envelope,registry, xmlwriter);
    else
      ss=new SectionVSerializer((LiteralEnvelope)envelope,registry, xmlwriter);
    payload=ss.serialize();

    /**
     * Make HTTP call and get back the response
     */
    //System.err.println("Request:"+ new String(payload));
    byte[] response = transport.call (payload);
    //System.err.println("Response:"+new String(response));

    /**
     * Return back a response as an Envelope.
     */
    LiteralEnvelope responseEnvelope = new LiteralEnvelope(envelope.getWSDLHolder());
    responseEnvelope.setPortType(envelope.getPortType());
    responseEnvelope.setOperation(envelope.getOperation());
    responseEnvelope.setMessage(responseEnvelope.getOperation().getOutputMessage());
    XmlParser parser = new XmlParser(new InputStreamReader( 
    new ByteArrayInputStream(response),"UTF-8"));  

    SOAPDeserializer sd = null;
    
    if (!this.isMessageLiteral(responseEnvelope))
    {
      //Section V deserializer.
      sd = new SectionVDeserializer(responseEnvelope,registry,parser,response);
      sd.deserialize();
    }
    else //if (this.isMessageLiteral(envelope))
    {
      //Call the literal deserializer here.
      //return null to get the class to compile;  
      sd=new LiteralDeserializer(responseEnvelope,registry,parser);
      sd.deserialize();
    }
    return responseEnvelope;
  } //invoke

  /**
   * Checks if the output message format uses
   * literal encoding.  Returns true if literal;
   * false otherwise.
   */
  private boolean isMessageLiteral(LiteralEnvelope le) 
  {
    WSDLHolder wh=le.getWSDLHolder();
    if (wh==null)
      return false;
    Binding binding=wh.getBinding(le.getPortType());
    for (int i=0; i<binding.getBindingOperationCount();i++) 
    {
      BindingOperation bo=binding.getBindingOperation(i);
      MessageFormat[] omf=bo.getOutputMessageFormat();
      if (omf!=null) 
      {
        for (int j=0; j<omf.length; j++) 
        {
          MessageFormat mf = omf[j];
          if (mf instanceof SOAPMessage &&
          ((SOAPMessage)mf).getMessageType()==SOAPMessage.BODY &&
          ((SOAPMessage)mf).getUse()==SOAPMessage.LITERAL)
            return true;
          else
            return false;
        }
      }//if
    }//for
    return false;
  }//isMessageLiteral

} /* com.wingfoot.soap.rpc.Call */
