package com.wingfoot.unittest;

/**
 * A simple web service that returns the long value
 * that is sent to the server
 */

public class EchoLong {
     
    public EchoLong(){}

    public Long echoLong(Long value) {
	return value;
    }
} //EchoLong
