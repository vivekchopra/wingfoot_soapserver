package com.wingfoot.unittest;

/**
 * A simple web service that returns the short value
 * that is sent to the server
 */

public class EchoShort {
     
    public EchoShort(){}

    public Short echoShort(Short value) {
	return value;
    }
} //EchoShort
