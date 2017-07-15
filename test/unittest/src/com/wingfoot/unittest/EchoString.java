package com.wingfoot.unittest;

/**
 * A simple web service that returns the string value
 * that is sent to the server
 */

public class EchoString {
     
    public EchoString(){}

     public String echoString(String value) {
         System.err.println("****The bean got:"+value+":");
	 return value;
     }

     public String echoString(String value, String name) {
	 return value + "::::" + name;
     }
} //EchoString
