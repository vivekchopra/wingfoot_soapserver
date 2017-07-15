/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */

package com.wingfoot.soap.encoding;
import com.wingfoot.soap.*;

/**
 * Encapsulates a byte array as a Base64 encoded String
 * and vice-versa. Users are encouraged to use instances
 * of this class to send binary data as base64 type.
 * @since 0.90
 * @author Kal Iyer
 */

public class Base64 {
    
  /* byte array encoded as a base64 String */
  private String base64;

  /* Raw un-encoded byte array */
  private byte[] raw;
    
  /**
   * Creates instance of Base64 with an
   * encoded string.  Converts the encoded
   * string to a byte array.
   * @since 0.90.
   * @param encodedString base64 encoded String
   * that is converted to raw bytes.
   * @throws SOAPException the constructor decodes
   * the encoded String.  An exception is thrown if
   * any problems are encountered while decoding the
   * String.
   */
  public Base64 (String encodedString) 
    throws SOAPException{
    //this.base64=encodedString.trim();
    this.base64=encodedString;

    /** Put code here to decode String to byte array **/
    raw = decode();
  }

  /**
   * Creates instance of Base64 with a
   * raw byte array.  Encodes the raw
   * byte array to a base64 String.
   * @since 0.90.
   * @param rawBytes raw byte array to
   * convert an encoded base64 String.
   */
  public Base64(byte[] rawBytes) {
    this.raw=rawBytes;

    /** Put code here to encode byte array to String **/
    base64 = encode();
  }
    
  /**
   * Returns the base64 encoded String version
   * of a raw byte array; null if no raw byte
   * array is provided in the constructor.
   * @since 0.90
   * @return the base64 encoded String
   */
  public String getEncodedString() {
    return base64;
  }

  /**
   * Returns the raw byte array version
   * of a base64 encoded String.
   * @since 0.90
   * @return byte[] representing the raw bytes
   * of a base64 encoded String; null if no 
   * encoded String is provided in the constructor.
   */
  public byte[] getBytes() {
    return raw;
  }

  /**
   * Returns the encoded String representation
   * of the byte array
   * @since 0.90
   * @return the base64 encoded String
   */
  public String toString() {
    return base64;
  }

  /** 
   * Encodes the byte array and returns a string
   */
  private String encode() {
    StringBuffer encoded = new StringBuffer();
    for (int i = 0; i < raw.length; i += 3) {
      encoded.append(encodeBlock(raw, i));
    }
    return encoded.toString();
  }

  private char[] encodeBlock(byte[] raw, int offset) {
    int block = 0;
    int slack = raw.length - offset - 1;
    int end = (slack >= 2) ? 2 : slack;
    for (int i = 0; i <= end; i++) {
      byte b = raw[offset + i];
      int neuter = (b < 0) ? b + 256 : b;
      block += neuter << (8 * (2 - i));
    }
    char[] base64 = new char[4];
    for (int i = 0; i < 4; i++) {
      int sixbit = (block >>> (6 * (3 - i))) & 0x3f;
      base64[i] = getChar(sixbit);
    }
    if (slack < 1) base64[2] = '=';
    if (slack < 2) base64[3] = '=';
    return base64;
  }

  private char getChar(int sixBit) {
    if (sixBit >= 0 && sixBit <= 25)
      return (char)('A' + sixBit);
    if (sixBit >= 26 && sixBit <= 51)
      return (char)('a' + (sixBit - 26));
    if (sixBit >= 52 && sixBit <= 61)
      return (char)('0' + (sixBit - 52));
    if (sixBit == 62) return '+';
    if (sixBit == 63) return '/';
    return '?';
  }

  /** 
   * Decodes a Base64 encoded String and returns 
   * a byte array
   * The encoded data is in a String named base64
   */
  
  private byte[] decode() throws SOAPException {
    /**
       int pad = 0;
       for (int i = base64.length() - 1; base64.charAt(i) == '='; i--)
       pad++;
       int length = base64.length() * 6 / 8 - pad;
       byte[] raw = new byte[length];
       int rawIndex = 0;
       for (int i = 0; i < base64.length(); i += 4) {
       int block = (getValue(base64.charAt(i)) << 18)
       + (getValue(base64.charAt(i + 1)) << 12)
       + (getValue(base64.charAt(i + 2)) << 6)
       + (getValue(base64.charAt(i + 3)));
       for (int j = 0; j < 3 && rawIndex + j < raw.length; j++)
       raw[rawIndex + j] = (byte)((block >> (8 * (2 - j))) & 0xff);
       rawIndex += 3;
       }
       return raw;
    **/
    /**
     * The first thing to do is to prepare a 
     * list of valid characters that can go in
     * a base64 encoded String.
     */
    byte[] codes =  getValidBytes();
    char[] data = base64.toCharArray();
    int tempLen=data.length;
    for (int ix=0; ix<data.length; ix++) {
      if ( (data[ix]>255)||codes[data[ix]]<0)
	--tempLen; //ignore non-valid chars and padding.
    } //for

    /*
     * Calculate required length:
     * 3 bytes for every 4 valid base64 chars;
     * plus 2 bytes if there are 3 extra base64 chars;
     * or plus 1 byte if ther are 2 extra;
     */
    int len = (tempLen/4)*3;
    if ((tempLen%4)==3) len+=2;
    if ((tempLen%4)==2) len+=1;
    byte[] out = new byte[len];

    int shift=0;
    int accum=0;
    int index=0;

    for (int ix=0; ix<data.length; ix++) {
      int value=(data[ix]>255)?-1:codes[data[ix]];
      if (value>=0) {
	accum<<=6;
	shift+=6;
	accum|=value;
	if (shift>=8) {
	  shift-=8;
	  out[index++]=(byte)((accum>>shift)&0xff);
	} //if
      } //if
    } //for

    if (index!=out.length) {
      throw new SOAPException ("Problems encountered while decoding Base64 string");
    }
    return out;
  } /*decode*/
  /**
     private int getValue(char c) {
     if (c >= 'A' && c <= 'Z') return c - 'A';
     if (c >= 'a' && c <= 'z') return c - 'a' + 26;
     if (c >= '0' && c <= '9') return c - '0' + 52;
     if (c == '+') return 62;
     if (c == '/') return 63;
     if (c == '=') return 0;
     return -1;
     }
  **/
  private byte[] getValidBytes() {
    byte[] codes=new byte[256];
    for (int i=0; i<256; i++) codes[i]=-1;
    for (int i='A'; i<='Z'; i++) codes[i]=(byte) (i-'A');
    for (int i='a'; i<='z'; i++) codes[i]=(byte) (26+i-'a');
    for (int i='0'; i<='9'; i++) codes[i]=(byte) (52+i-'0');
    codes['+']=62;
    codes['/']=63;
    return codes;
  }

} /* com.wingfoot.soap.encoding.Base64 */
