package com.wingfoot.unittest;

/**
 * A simple web service that returns the Interger value
 * that is sent to the server
 */

public class EchoInt {
     
    public EchoInt(){}

    public Integer echoInt(Integer value) {
	return value;
    }
} //EchoInt
