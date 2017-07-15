package com.wingfoot.unittest;

import com.wingfoot.soap.encoding.*;

/**
 * A simple web service that returns the string value
 * that is sent to the server
 */

public class EchoHexBinary {
     
    public EchoHexBinary(){}

     public HexBinary echoHexBinary(HexBinary value) {
	 return value;
     }
} //EchoString
