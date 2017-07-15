/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
*/


package com.wingfoot.interop.interopGroupB;
import com.wingfoot.soap.encoding.*;

public class EmployeeBeanNestedArray implements WSerializable {
    
    /**
     private String name;
     private Integer age;
     private com.wingfoot.soap.encoding.Float salary;
    **/
     private Object name;
     private Object age;
     private Object salary;
     private Object stringArray;

     public int getPropertyCount() {
          return 4;
     }

     public void setPropertyAt(Object value, int index) {
          if (index==0) 
	       this.name= value;
          else if (index==1)
	       age =  value;
          else if (index==2)
	       salary = value;
          else if (index==3)
	       stringArray = value;
     }

     public String getPropertyName (int index) {
          if (index == 0)
	       return "varString";
          else if (index ==1)
	       return "varInt";
          else if (index ==2)
	       return "varFloat";
          else if (index ==3)
	       return "varArray";
          else return "";
     }

     public void removeProperty (int index) {
          if (index == 0)
	       name=null;
          else if (index ==1)
	       age=null;
          else if (index ==2)
	       salary=null;
          else if (index ==3)
	       stringArray=null;
     }

     public Object getPropertyValue(int index) {
          if (index ==0)
	       return name;
           else if (index == 1)
	        return age;
           else if (index == 2)
	        return salary;
           else if (index == 3)
	        return stringArray;
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
          else if (name.trim().equals("varArray"))
	       stringArray = value;
     }

     public String getName() { return (String) name; }
     public Integer getAge() { return (Integer) age; }
     public Float getSalary() { return 
          (Float) salary; }
     public Object getArray() {
          return  stringArray;
     }

     public void setName(String name) {
         this.name=name;
     }

     public void setAge(int age) {
         this.age = new Integer(age);
     }

     public void setSalary(Float salary) {
         this.salary=salary;
     }

     public void setArray(String[] array) {
         this.stringArray = array;
     }
} //class
