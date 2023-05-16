package org.pentaho.di.trans.steps.crawlerinput;

/*********************************************
    Copyright (c) 2001 by Daniel Matuschek
*********************************************/

/**
 * A ByteBuffer implements a growable byte array. You can simple
 * add bytes like you do it using a Vector, but internally the buffer
 * is implemented as a real array of bytes. This increases memory usage.
 *
 * @author Daniel Matuschek
 * @version $Id $
 */
public class ByteBuffer {
  
  protected final int INITIALSIZE=1024;
  
  protected int used = 0;
  protected int size = 0;
  protected byte[] buff =null;
  
  /**
   * Initializes a new ByteBuffer object and creates
   * a temporary buffer array of a predefined initial size.
   * If you want to set your own initial size, use the <code>setSize</code>
   * method after initializing the object.
   * 
   */
  public ByteBuffer() {
    size=INITIALSIZE;
    buff=new byte[INITIALSIZE];
  }


  /**
   * Appends a byte to the end of the buffer
   *
   * If the currently reserved memory is used, the size of the 
   * internal buffer will be doubled.
   * In this case the memory usage will temprary increase by factor 3
   * because it need a temporary storage for the old data.
   *
   * Be sure that you have enough heap memory !
   *
   * @param b byte to append
   */
  public void append(byte b) {
    if (used >= size) {
      doubleBuffer();
    }
    
    buff[used]=b;
    used++;
  }

  /**
   * @return the number of bytes stored in the buffer
   */
  public int length() {
    return used;
  }


  /**
   * @return the buffer contents as a byte array
   */
  public byte[] getContent() {
    byte[] b = new byte[used];
    for (int i=0; i<used; i++) {
      b[i]=buff[i];
    }
    return b;
//    buff = b;
//    return buff;
  }

  /**
   * removes all contents in the buffer
   */
  public void clean() {
    used=0;
  }


  /**
   * Sets the size of the internal buffer to
   * the given value. This is useful, if the size of the
   * data that should be stored is known.
   * @param size size of the buffer in Bytes
   */
  public void setSize(int size) {

    // if we have already used more data, ignore it !
    if (size < used) {
      return;
    }

    this.size=size;

    // create a new (larger) array
    byte[] newBuff = new byte[size];
    
    // copy contents
    for (int i=0; i<used; i++) {
      newBuff[i]=buff[i];
    }

    buff=newBuff;
  }


  /**
   * Print the buffer content as a String (use it for debugging only !)
   * @return a String containing every byte in the buffer as a character
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(buff.length);
    for (int i=0; i<used; i++) {
      sb.append(buff[i]);
    }
    return sb.toString();
  }


  /**
   * doubles the size of the internal buffer
   */
  protected void doubleBuffer() {
   // increase size
    setSize(size*2);
  }



}
