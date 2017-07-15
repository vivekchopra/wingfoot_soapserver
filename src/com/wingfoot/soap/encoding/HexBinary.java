/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */

package com.wingfoot.soap.encoding;

import java.io.*;
import com.wingfoot.soap.*;
/**
 * Encapsulates a byte array as a Hexadecimal String
 * and vice-versa. Users are encouraged to use instances
 * of this class to send binary data in Hex format.
 * @since 0.90
 * @author Kal Iyer
 */

public class HexBinary {
    
  /* byte array encoded as a Hex String */
  private String hex;

  /* Raw un-encoded byte array */
  private byte[] raw;
    
  /**
   * Creates instance of HexBinary.  Converts 
   * the string to a byte[] format.
   * @since 0.90.
   * @param encodedString hex encoded String
   * that is converted to raw bytes.
   * @throws SOAPException - if any error occus 
   * while decoding the encoded string.
   */
  public HexBinary (String encodedString) throws SOAPException {
    this.hex=encodedString.trim();
    /** Put code here to decode String to byte array **/
    raw = decode();
  }

  /**
   * Creates instance of HexBinary.  
   * Encodes the raw byte array to a 
   * hexadecimal String.
   * @since 0.90.
   * @param rawBytes raw byte array to
   * convert a hex String.
   */
  public HexBinary(byte[] rawBytes) {
    this.raw=rawBytes;

    /** Put code here to encode byte array to String **/
    hex = encode();
  }
    
  /**
   * Returns the hexadecimal String version
   * of a raw byte array; null if no raw byte
   * array is provided in the constructor.
   * @since 0.90
   * @return the hexadecimal encoded String
   */
  public String getEncodedString() {
    return hex;
  }

  /**
   * Returns the raw byte array version
   * of a hexadecimal encoded String.
   * @since 0.90
   * @return byte[] representing the raw bytes
   * of a hex encoded String; null if no 
   * encoded String is provided in the constructor.
   */
  public byte[] getBytes() {
    return raw;
  }

  /**
   * Returns the encoded String representation
   * of the byte array
   * @since 0.90
   * @return the hex encoded String
   */
  public String toString() {
    return hex;
  }

  /** 
   * Encodes the byte array and returns a string
   */
  private String encode() {
    if (raw==null)
      return null;
    StringBuffer sb=new StringBuffer();
    for (int i=0; i<raw.length; i++) {
      String tmpHex=
	Integer.toHexString(raw[i]).toUpperCase();
      if (tmpHex.length()<2)
	sb.append("0");
      sb.append(tmpHex);
    }
    return sb.toString();
  }

  /** 
   * Decodes a hex encoded String and returns 
   * a byte array
   */

  private byte[] decode() throws SOAPException {
    if (hex==null) return null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    for (int i=0; i<hex.length(); i+=2) {
      char c1 = hex.charAt(i);
      if ((i+1) >= hex.length()) 
	throw new SOAPException("Incorrect number of digits in string");
            
      char c2 = hex.charAt(i+1);
      byte b=0;

      if (c1>='0' && c1<='9') 
	b+=(c1-'0')*16;
      else if (c1>='a' && c1<='f') 
	b+=((c1-'a')+10)*16;
      else if (c1>='A' && c1<='F') 
	b+=((c1-'A')+10)*16;
      else 
	throw new SOAPException("Illegal character in hex string");
            
      if (c2>='0' && c2<='9') 
	b+= c2-'0';
      else if (c2>='a' && c2<='f') 
	b+=(c2-'a')+10;
      else if (c2>='A' && c2<='F') 
	b+=(c2-'A')+10;
      else 
	throw new SOAPException("Illegal character in hex string");
            
      baos.write(b);
    } //for
    return baos.toByteArray();
  } /*decode*/

} /* com.wingfoot.soap.encoding.Base64 */
