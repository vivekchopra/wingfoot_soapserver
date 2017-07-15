/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
*/

package com.wingfoot.unittestB;

import com.wingfoot.soap.encoding.*;

/**
 * Class which encapsulates the DirectoryCategory from the search
 * structure returned from Google.  A DirectoryCategory is a 
 * data structure returned in the payload.  This is encapsulated
 * by a custom java class.  Since KVM does not have the full blown
 * reflection/interospection, the class has to implement WSerializable.
 */
public class DirectoryCategory implements WSerializable {

    private final int DIRECTORY_CATEGORY_PROPERTY_COUNT  = 2;

    /**
     * It is always better to declare the properties
     * as Object and type cast to the proper object
     * before sending it over.  This is because some 
     * SOAP servers do not decorate an element with 
     * data type.  Wingfoot, takes care of this case
     * by allowing the user to specify a element map.
     * If the element is also not mapped to a data type
     * then the toolkit really does not know how to handle
     * the objects.  In such an instance the toolkit
     * returns scalar data types as String and Vector
     * data types as UntypedObject.  Hence, to play it
     * safe declare the properties as Object, to avoid
     * any ClassCastException. 
     * If absolutely sure that the server decorates
     * each element with a data type, then go ahead
     * and declare the property with the correct
     * data type (String in this case).
     */

    private Object fullViewableName = null;
    private Object specialEncoding = null;

    public DirectoryCategory(){
    } //constructor
    
    public void setPropertyAt(Object o, int index) {
    }

    public int getPropertyCount() {
	return DIRECTORY_CATEGORY_PROPERTY_COUNT;
    }

    public String getPropertyName (int index) {
	if (index == 0)
	    return "fullViewableName";
	else if (index == 1)
	    return "specialEncoding";
	else
	    return "";
    }
    
    public void removeProperty (int index) {
	if (index == 0)
	    fullViewableName = null;
	else if (index == 1)
	    specialEncoding = null;
    }

    public Object getPropertyValue(int index){
	
	if (index == 0)
	    return fullViewableName;
	
	else if (index == 1)
	    return specialEncoding;
	else
	    return "";
    }

    public void setProperty(String name, Object value) {
	
	if (name.trim().equals("fullViewableName"))
	    this.fullViewableName = value;
	else if (name.trim().equals("specialEncoding"))
	    this.specialEncoding = value;
    }


    /* the getters */
    public String getFullViewableName(){
	return (String) fullViewableName;
    }
    public String getSpecialEncoding(){
	return (String) specialEncoding;
    }

}
