package org.pentaho.di.trans.steps.crawlerinput.cookie;
/*********************************************
    Copyright (c) 2001 by Daniel Matuschek
*********************************************/
                                         
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * This object represents an HTTP cookie for a browser.
 * It can interpret both Netscape and RFC cookies
 * 
 * @author Daniel Matuschek 
 * @version $Id $
 */
public class Cookie
{
  /** HTTP Set-Cookie response header (not case sensitive) */
  final static String HEADER_SETCOOKIE="Set-Cookie:";

  /** Script document.cookie (not case sensitive) */
  final public static String DOCUMENT_COOKIE="document.cookie=";
  
  /** Cookie name */
  private String name;

  /** Cookie value */
  private String value=null;

  /** 
   * Life time in seconds, -1 means "expire, if browser exits" <br />
   * this is only useful for RFC 2109 cookie, because Netscape cookies
   * do not have a maxAge field. This value will only be used to 
   * create the internal expireDate value.
   */
  private long maxAge=-1;

  /** Comment */
  private String comment="";

  /** Domain */
  private String domain=null;

  /** Path */
  private String path="/";

  /** Secure ? */
  private boolean secure=false;

  /**
   * expire date, default is "never" for cookies without explicit
   * exipration date
   */
  private Date expireDate=new Date(Long.MAX_VALUE);

  /** 
   * Cookie version <br />
   * version=0 refers to the Netscape cookie specification <br />
   * version=1 refers to the RFC 2109 Cookie specification <br />
   * <br />
   * @see <a href="http://home.netscape.com/newsref/std/cookie_spec.html">
   * Netscape Cookie specification</a><br />
   */
  private int version=0;


  /**
   * Default constructor, creates an empty cookie
   */
  public Cookie() {
  }


  /**
   * Constructor that initializes a cookie from a HTTP Set-Cookie: header
   * @param setCookie a HTTP Set-Cookie: header line (including Set-Cookie)
   * @param u there URL of the HTTP document where this cookie was set from
   * this is needed, if no "domain" field is given in the cookie. 
   * It will be ignored otherwise
   * @exception CookieException if the given setCookie String is not a valid
   * HTTP Set-Cookie response header
   */
  public Cookie(String setCookie, URL u) 
    throws CookieException
  {
    this();

    String cookieHeader = null;
    String host = "";
    StringTokenizer tokens = null;

    // does is start with "Set-Cookie" ?
    if (setCookie.substring(0,HEADER_SETCOOKIE.length()).equalsIgnoreCase(HEADER_SETCOOKIE)) {
      cookieHeader = setCookie.substring(HEADER_SETCOOKIE.length());
    }else if (setCookie.substring(0,DOCUMENT_COOKIE.length()).equalsIgnoreCase(DOCUMENT_COOKIE)) {
      cookieHeader = setCookie.substring(DOCUMENT_COOKIE.length());
      if (cookieHeader.indexOf("\"") > -1)
    	  cookieHeader = cookieHeader.substring(cookieHeader.indexOf("\"")+ 1,cookieHeader.lastIndexOf("\""));
    }     
    else {
      throw new CookieException("Not a Set-Cookie header");
    }

    // set defaults from the URL
    if (u != null) {
      this.domain = u.getHost().toLowerCase();
      host = this.domain;
    }

    // tokenize setcookie request
    tokens = new StringTokenizer(cookieHeader,";");

    // there must be at least ONE token (name=value)
    if (tokens.countTokens() < 1) {
      throw new CookieException("Cookie contains no data");
    } else {
      String field = tokens.nextToken();
      int pos = field.indexOf('=');
      if (pos <= 0) {
	throw new CookieException("First field not in the format NAME=VALUE"
				  +" but got "+field);
      } else {
	name = field.substring(0,pos).trim();
	value = field.substring(pos+1);
      }
    }

    // parse all other fields
    while (tokens.hasMoreTokens()) {
      String field = tokens.nextToken();
      String fieldname="";
      String fieldvalue="";

      int pos = field.indexOf('=');
      if (pos <= 0) {
	fieldname = field.trim();
	fieldvalue="";
      } else {
	fieldname = field.substring(0,pos).trim();
	fieldvalue = field.substring(pos+1).trim();
      }

      if (fieldname.equalsIgnoreCase("comment")) {
	// 
	// COMMENT
	//
	this.comment = fieldvalue;
      } else if (fieldname.equalsIgnoreCase("domain")) {
	//
	// DOMAIN
	//
	String domainvalue = fieldvalue.toLowerCase();
	// check if the domain is allowed for the current URL !
	if ((host.equals("")) 
	    || (host.endsWith(domain))) {
	  this.domain=domainvalue;
	} else {
	  throw new CookieException("Not allowed to set a cookie for domain "
				    +domainvalue+" from host "+host);
	}
      } else if (fieldname.equalsIgnoreCase("jmfdomain")) {
	//
	// JMFDOMAIN
	//
	String domainvalue = fieldvalue.toLowerCase();
	// check if the domain is allowed for the current URL !
	if ((host.equals("")) 
	    || (host.endsWith(domain))) {
	  this.domain=domainvalue;
	} else {
	  throw new CookieException("Not allowed to set a cookie for domain "
				    +domainvalue+" from host "+host);
	}
      } else if (fieldname.equalsIgnoreCase("path")) {
	// 
	// PATH
	//
	this.path=path;
      } else if (fieldname.equalsIgnoreCase("secure")) {
	//
	// SECURE
	//
	this.secure = true;
      } else if (fieldname.equalsIgnoreCase("max-age")) {
	//
	// MAX-AGE
	//
	try {
	  this.maxAge = Integer.parseInt(fieldvalue);
	} catch (NumberFormatException e) {
	  throw new CookieException("max-age must be integer, but is "
				    +fieldvalue);	  
	}
	
	if (maxAge >= 0) {
	  this.expireDate = new Date(System.currentTimeMillis()
				     +maxAge*1000);
	} else {
	  this.expireDate = new Date(Long.MAX_VALUE);
	}
      } else if (fieldname.equalsIgnoreCase("expires")) {
	//
	// EXPIRES
	//
	String dateStr = null;
	java.text.SimpleDateFormat df[] = new java.text.SimpleDateFormat[2];

	// possible date formats
	// thanks to Scott Woodson for the feedback
	df[0] = new java.text.SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z",
					       Locale.US);
	df[1] = new java.text.SimpleDateFormat("dd MMM yyyy HH:mm:ss z",
					       Locale.US);
	
	int commapos = fieldvalue.indexOf(",");
	if (commapos < 0) {
	  throw new CookieException("Expires field does not contain "
				    +"a comma, value is "+fieldvalue);
	}
	dateStr = fieldvalue.substring(commapos+1).trim();
	boolean foundDate = false;

	for (int i=0; i<df.length; i++) {
	  try {
	    this.expireDate = df[i].parse(dateStr);
	    i=df.length+1;
	    foundDate=true;
	  } catch (java.text.ParseException e) {};
	}
	
	// found a valid date ?
	if (! foundDate) {
	  throw new CookieException("Expires field can't be parses as date, "
				    +"value is "+dateStr);
	}

      } else if (fieldname.equalsIgnoreCase("version")) {
	// 
	// VERSION
	//
	try {
	  this.version=Integer.parseInt(fieldvalue);
	} catch (NumberFormatException e) {
	  throw new CookieException("Version must be integer, but is "
				    +fieldvalue);
	}

	if (version > 1) {
	  throw new CookieException("Only version 0 and 1 supported yet, "
				    +"but cookie used version "+version);
	}
      }
    }

  }
  

  /**
   * Is this cookie valid ?
   * @return true if the cookie is valid, false if it is expired
   */
  public boolean isValid() {
    Date current = new Date();
    return current.before(expireDate);
  }


  /**
   * Is this cookie valid for the given URL ?
   * That means, it is not expired and host and path matches the given URL
   * @return true if this cookie is valid for the given URL, false otherwise
   */
  public boolean isValid(URL u) {
    String urlhost = u.getHost().toLowerCase();
    String urlpath = u.getPath();

    return (isValid() 
	    && urlhost.endsWith(this.domain)
	    && urlpath.startsWith(path));
  }

  /**
   * Does this Cookie overwrite another cookie ? 
   * A Cookie overwrites another one, if they have the same
   * name, domain and path. It doesn't matter, if expireDate or value of
   * the cookie are different !
   */
  public boolean overwrites(Cookie c) {
    return (this.domain.equals(c.domain)
	    && this.path.equals(c.path)
	    && this.name.equals(c.name));
  }


  /**
   * Gets the cookie name and value as NAME=VALUE pair
   * @return a string in the format NAME=VALUE
   */
  public String getNameValuePair() {
    return this.name+"="+this.value;
  }


  /** 
   * Convert the cookie to a String. Format is not defined and may change
   * without notice. Use it for debugging and logging purposes only !
   * @return a String representation of this cookie
   */
  public String toString() {
    return this.name+"="+this.value+" (Comment="+this.comment
      +", Version="+this.version+", domain="+this.domain
      +", path="+this.path
      +", expires "
      +java.text.DateFormat.getDateTimeInstance().format(this.expireDate)+")";
  }

}