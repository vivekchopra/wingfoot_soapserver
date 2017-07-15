/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
*/


package com.wingfoot.unittestB;

/**
 * This class represents a web service containing methods specified for
 * round two of the Unit Test
 * The focus of on:
 *       Array
 *       2 Dimensional Array
 *       Hashtable
 *       Vector
 *       Java Bean - Structs
 */

import java.util.*;
import com.wingfoot.soap.encoding.*;
public class UnitTestB {

    public UnitTestB(){}

    /**
     * This method will echo a String Array
     */
    public Object[] echoStringArray(Object[] value) throws Exception {
	return value;
    }

    /**
     * This method will echo a Integer Array
     */
    public Object[] echoIntegerArray(Object[] value){
	return value;
    }

    /**
     * This method will echo a Float Array
     */
    public Object[] echoFloatArray(Object[] value){
	return value;
    }

    /**
     * This method will echo a Byte Array
     */
    public byte[] echoByteArray(byte[] value){
	return value;
    }

    /**
     * This method will echo a Short Array
     */
    public Object[] echoShortArray(Object[] value){
	return value;
    }

    /**
     * This method will echo a Long Array
     */
    public Object[] echoLongArray(Object[] value){
	return value;
    }

    /**
     * This method will echo a Double Array
     */
    public Object[] echoDoubleArray(Object[] value){
	return value;
    }

    /**
     * 2 dimensional arrays are tested in the following methods
     */

    /**
     * This method will echo a 2 dimensional String Array
     */
    public Object[] echoTwoStringArray(Object[] value){
	return value;
    }

    /**
     * This method will echo a 2 dimensional Integer Array
     */
    public Object[][] echoTwoIntegerArray(Object[][] value){
	return value;
    }

    /**
     * This method will echo a 2 dimensional Float Array
     */
    public Object[][] echoTwoFloatArray(Object[][] value){
	return value;
    }

    /**
     * This method will echo a 2 dimensional Byte Array
     */
    public Object[][] echoTwoByteArray(Object[][] value){
	return value;
    }

    /**
     * This method will echo a 2 dimensional Short Array
     */
    public Object[][] echoTwoShortArray(Object[][] value){
	return value;
    }

    /**
     * This method will echo a 2 dimensional Long Array
     */
    public Object[][] echoTwoLongArray(Object[][] value){
	return value;
    }

    /**
     * This method will echo a 2 dimensional Double Array
     */
    public Object[][] echoTwoDoubleArray(Object[][] value){
	return value;
    }

    /**
     * This method will echo the contents of a Java Bean
     */
    public EmployeeBean echoEmployeeBean(EmployeeBean value){
	return value;
    }

    public DirectoryCategory echoDirectorySearch(DirectoryCategory value){
	return value;
    }
    /**
     * This method will echo the contents of a Hashtable
     */
    public Hashtable echoHashtable(Hashtable value){
	return value;
    }

    /**
     * This method will echo the contents of a vector
     */
    public Vector echoVector(Vector value) {
	return value;
    }

    public void echoVoid(){
    }
}
