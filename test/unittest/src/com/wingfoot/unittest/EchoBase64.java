package com.wingfoot.unittest;

import com.wingfoot.soap.encoding.Base64;

/**
 * A simple web service that returns the string value
 * that is sent to the server
 */

public class EchoBase64 {
     
    public EchoBase64(){}

     public Base64 echoBase64(Base64 value) {
	 return value;
     }
} //EchoString
