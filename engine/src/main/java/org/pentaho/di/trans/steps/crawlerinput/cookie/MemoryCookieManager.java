package org.pentaho.di.trans.steps.crawlerinput.cookie;

/*********************************************
    Copyright (c) 2001 by Daniel Matuschek
*********************************************/

import java.util.Vector;
import java.net.URL;
                                         
/**
 * This class is a container for storing cookies in 
 * the memory. It will automatically expire old cookies.
 * 
 * @author Daniel Matuschek 
 * @version $Id $
 */
public class MemoryCookieManager implements CookieManager
{

  /**
   * an internal thread that will be used to clean up old cookies
   */
  class CleanupThread extends Thread {
    MemoryCookieManager cm = null;
    boolean finished = false;

    public CleanupThread(MemoryCookieManager cm) {
      this.cm = cm;
      this.setDaemon(true);
    }

    /** stop cleanup and finish this thread */
    public void finish() {
      this.finished = true;
    }

    public void run() {
      while (! finished) {
	// cleanup every minute
	try {
	  sleep(60000);
	} catch (InterruptedException e) {
	  this.finished=true;
	}
	cm.cleanUpExpired();
      }
    }
  }


  /** List of stored cookies */
  private Vector cookies;

  /** The background thread for cookie expiration */
  private CleanupThread ct = null;



  /**
   * Default constructor, initializes a new CookieManager
   * that has no cookies stored.
   * It also starts a CleanUp thread that will periodically delete
   * expired cookies.
   */
  public MemoryCookieManager() {
    cookies = new Vector();

    // start cleanup thread as a daemon
    CleanupThread ct = new CleanupThread(this);
    ct.start();
  }


  /** 
   * Add this cookie. If there is already a cookie with the same name and
   * path it will be owerwritten by the new cookie.
   * @param cookie a Cookie that will be stored in this cookie manager
   */
  public void add(Cookie cookie) {
    for (int i=0; i<cookies.size(); i++) {
      Cookie oldcookie = (Cookie)(cookies.elementAt(i));
      if (cookie.overwrites(oldcookie)) {
	cookies.removeElementAt(i);
	i--;
      }
    }
    cookies.add(cookie);
  }


  /** 
   * How many cookies are currently stored in this CookieManager ?
   * @return the number of stored Cookies
   */
  public int countCookies() {
    return this.cookies.size();
  }


  /** 
   * Get the cookie values for the given URL.
   * @return a String containing a list of NAME=VALUE pairs (separated by
   * semicolon) containing all cookie values that are valid for the
   * given URL, <br/ >
   * null if no cookies can be found for this URL
   */
  public String cookiesForURL(URL u) {
    final String SEP = "; ";
    StringBuffer sb = new StringBuffer();
    
    for (int i=0; i<cookies.size(); i++) {
      Cookie c = (Cookie)(cookies.elementAt(i));
      if (c.isValid(u)) {
	sb.append(c.getNameValuePair());
	sb.append(SEP);
      }
    }

    if (sb.length() > 0) {
      // ignore the last "; "
      return sb.substring(0,sb.length()-SEP.length());
    } else {
      return null;
    }
  }


  /**
   * Remove all stored cookies
   */
  public void clear() {
    this.cookies.clear();
  }


  /**
   * Cleans up expired cookies
   */
  protected void cleanUpExpired() {
    for (int i=0; i<cookies.size(); i++) {
      Cookie c = (Cookie)(cookies.elementAt(i));

      // if this cookie has expired, remove it
      if (! c.isValid()) {
	cookies.removeElementAt(i);
	i--;
      }
    }
  }

}