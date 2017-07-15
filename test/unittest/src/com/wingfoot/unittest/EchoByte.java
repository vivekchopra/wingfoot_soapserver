package com.wingfoot.unittest;

/**
 * A simple web service that returns the byte value
 * that is sent to the server
 */

public class EchoByte {
     
    public EchoByte(){}

     public Byte echoByte(Byte value) {
	 return value;
     }
} //EchoByte
