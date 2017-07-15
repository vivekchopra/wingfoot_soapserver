package com.wingfoot.unittest;

import java.util.Date;
/**
 * A simple web service that returns the date value
 * that is sent to the server
 */

public class EchoDate {
     
    public EchoDate(){}

    public Date echoDate(Date value) {
        System.err.println("***Date in the bean is: " + value);
	return value;
    }
    
} //EchoDate
