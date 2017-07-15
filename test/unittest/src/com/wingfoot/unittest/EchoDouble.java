package com.wingfoot.unittest;

/**
 * A simple web service that returns the float value
 * that is sent to the server
 */

public class EchoDouble {
     
    public EchoDouble(){}

    public Double echoDouble(Double value) {
	return value;
    }
    
} //EchoDouble
