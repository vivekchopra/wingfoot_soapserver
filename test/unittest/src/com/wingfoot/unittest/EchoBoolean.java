package com.wingfoot.unittest;

/**
 * A simple web service that returns the boolean value
 * that is sent to the server
 */

public class EchoBoolean {
     
    public EchoBoolean(){}

    public Boolean echoBoolean(Boolean value) {
	return value;
    }
    
} //EchoBoolean
