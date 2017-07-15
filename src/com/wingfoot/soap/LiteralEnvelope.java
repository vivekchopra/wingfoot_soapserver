package com.wingfoot.soap;
import com.wingfoot.*;
import com.wingfoot.wsdl.*;
import com.wingfoot.soap.*;
import com.wingfoot.wsdl.Message;
import com.wingfoot.wsdl.soap.*;
import java.util.*;
/**
 * SOAP envelope that encapsulates an envelope
 * that uses literal encoding.
 */
public class LiteralEnvelope extends SOAPElement //implements IEnvelope
{ 
  private WSDLHolder wsdlHolder;
  private PortType portType;
  private Operation operation;
  private Message message;
  private Vector parameter;
  private Fault fault;
  private String schema=Constants.SOAP_SCHEMA;
  private String schemaInstance=Constants.SOAP_SCHEMA_INSTANCE;
  //private String encodingStyle;
  /**
   * Creates a Literal envelope.
   */
  public LiteralEnvelope(WSDLHolder wsdlHolder)
  {
    this.wsdlHolder=wsdlHolder;
    super.addAttribute ("xmlns:SOAP-ENV", Constants.SOAP_NAMESPACE);
    super.addAttribute ("xmlns:xsd", Constants.SOAP_SCHEMA);
    super.addAttribute ("xmlns:xsi", Constants.SOAP_SCHEMA_INSTANCE);
  }

    /**
   * Retrieves the QName that encapsulates a PortType.
   * The PortType contains the Operation that is being
   * worked on.
   * @return PortType encapsulating a PortType; null if the 
   * PortType has not been set.
   */
  public PortType getPortType()
  {
    return portType;
  }

  /**
   * Sets the QName that encapsulates a PortType.  The
   * PortType contains the Operation that is being
   * worked on. 
   * @param newPortType PortType that represents the PortType
   * in WSDL.
   */
  public void setPortType(PortType newPortType)
  {
    portType = newPortType;
  }

  /**
   * Returns the Operation whose Parts are
   * to be repsented as SOAP payload.
   * @return Operation
   */
  public Operation getOperation()
  {
    return operation;
  }

  /**
   * Sets the Operation whose Parts are
   * to be represented as SOAP payload.
   * @param newOperation Operation to encapsulate
   * as SOAP.
   */
  public void setOperation(Operation newOperation)
  {
    operation = newOperation;
  } 

  public Message getMessage()
  {
    return message;
  }

  public void setMessage(Message newMessage)
  {
    message = newMessage;
  }

  public void setParameter(Object parameter) 
  {
    if (this.parameter==null)
      this.parameter=new Vector();
    this.parameter.add(parameter);
  }
  
  public Vector getParameter() 
  {
    return this.parameter;
  }

  public Object getParameter(int index)
  {

    if (this.parameter==null || this.parameter.size()<index+1)
      return null;
    return this.parameter.elementAt(index);
  }

  public WSDLHolder getWSDLHolder() 
  {
    return this.wsdlHolder;
  }

  public Fault getFault()
  {
    return fault;
  }

  public void setFault(Fault newFault)
  {
    fault = newFault;
  }

  public boolean isFaultGenerated() 
  {
    return this.fault==null?false:true;
  }

  public String getSchema()
  {
    return schema;
  }

  public void setSchema(String newSchema)
  {
    schema = newSchema;
  }

  public String getSchemaInstance()
  {
    return schemaInstance;
  }

  public void setSchemaInstance(String newSchemaInstance)
  {
    schemaInstance = newSchemaInstance;
  }

  public int getParameterCount()
  {
    if(parameter == null)
      return 0;
    else
      return parameter.size();
  }

  /**
   * Returns the encoding style for the message from 
   * the WSDL.  If the encoding style is not specified
   * (as will be the case for a document style encoding)
   * then null is returned.
   * @return String the encoding style; null if the
   * encoding style attribute is not set in WSDL.
   */
  public String getEncodingStyle()
  {
    //Get the binding from the operation.
    Binding b = wsdlHolder.getBinding(this.portType);
    BindingOperation bo=b.getBindingOperation(this.getOperation());

    /**
     * Determine if the Message in the WSDL is inputMessage
     * or outputMessage.  The encodingStyle is correspondingly
     * retrieved from the WSDL.
     */
     if (this.operation.getInputMessage()!=null
     && this.operation.getInputMessage().equals(this.message)) 
      return ((SOAPMessage)(bo.getInputMessageFormat())[0]).getEncodingStyle();
     else if (this.operation.getOutputMessage()!=null 
     && this.operation.getOutputMessage().equals(this.message)) 
      return ((SOAPMessage)(bo.getOutputMessageFormat())[0]).getEncodingStyle();
     else
      return null;
  }

/*
  public void setEncodingStyle(String newEncodingStyle)
  {
    encodingStyle = newEncodingStyle;
    if (encodingStyle != null)
      super.addAttribute ("SOAP-ENV:encodingStyle",encodingStyle);
    else
      super.removeAttribute("SOAP-ENV:encodingStyle");
  }
  */
  
}//LiteralEnvelope