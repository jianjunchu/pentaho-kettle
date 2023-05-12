package org.pentaho.di.trans.steps.crawlerinput.cookie;
/*********************************************
    Copyright (c) 2001 by Daniel Matuschek
*********************************************/
                                         
/**
 * A generic exception for cookie problems.
 *
 * @author Daniel Matuschek 
 * @version $Id $
 */
public class CookieException extends Exception {
  
  public CookieException(String message) {
    super(message);
  }

}