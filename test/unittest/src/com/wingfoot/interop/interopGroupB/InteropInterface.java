/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
*/


package com.wingfoot.interop.interopGroupB;
import java.util.*;
public interface InteropInterface {
     
     public void setTransport(String url);
     public void setSchema(String schema);
     public void setSchemaInstance(String schemaInstance);
     public String run() throws Exception;
     public void setElementMap (Vector elementMap) ;
     public void setTypeMap (Vector typeMap) ;
} /* interface */
