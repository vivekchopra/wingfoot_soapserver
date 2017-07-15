package com.wingfoot.unittest;

/**
 * A simple web service that returns the string value
 * that is sent to the server
 */

import com.wingfoot.interop.interop.*;

public class TestInterop {
     
    public TestInterop(){}

    public void testInterop(String value) {
	//The value is the xml file - plus location
	System.err.println("****The bean got:"+value+":");
	//return value;
	try {
	    InteropClient ip = new InteropClient();
	    ip.init(value);
	} catch (Exception e){
	    System.err.println("error: " + e.getMessage());
	    e.printStackTrace();
	}
    }

    public void testInteropB(String value) {
	//The value is the xml file - plus location
	System.err.println("****The bean got:"+value+":");
	//return value;
	try {
	    InteropClient ip = new InteropClient();
	    ip.init(value);
	} catch (Exception e){
	    System.err.println("error: " + e.getMessage());
	    e.printStackTrace();
	}
    }
    
} //TestInterop
