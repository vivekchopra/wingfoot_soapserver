/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
*/

package com.wingfoot.interop;
import com.wingfoot.soap.encoding.*;

/**
 * This Class is used for the testing of JavaBeans
 */

public class EmployeeBean implements WSerializable {
    
     private Object name;
     private Object age;
     private Object salary;

     public int getPropertyCount() {
          return 3;
     }
     
     public void setPropertyAt(Object value, int index) {
          if (index==0) 
	       this.name= value;
          else if (index==1)
	       age =  value;
          else if (index==2)
	       salary = value;
     }

     public String getPropertyName (int index) {
          if (index == 0)
	       return "varString";
          else if (index ==1)
	       return "varInt";
          else if (index ==2)
	       return "varFloat";
          else return "";
     }

     public void removeProperty (int index) {
          if (index == 0)
	       name=null;
          else if (index ==1)
	       age=null;
          else if (index ==2)
	       salary=null;
     }

     public Object getPropertyValue(int index) {
          if (index ==0)
	       return name;
           else if (index == 1)
	        return age;
           else if (index == 2)
	        return salary;
           else 
	        return "";
     }

     public void setProperty (String name, Object value) {
          if (name.trim().equals("varString")) 
	       this.name= value;
          else if (name.trim().equals("varInt"))
	       age =  value;
          else if (name.trim().equals("varFloat"))
	       salary = value;
     }

     public String getName() { return (String) name; }
     public Integer getAge() { return (Integer) age; }
     public Float getSalary() { return (Float) salary; }

     public void setName(String name) {
         this.name=name;
     }

     public void setAge(Integer age) {
         this.age = age;
     }

     public void setSalary(Float salary) {
         this.salary=salary;
     }

} //class
