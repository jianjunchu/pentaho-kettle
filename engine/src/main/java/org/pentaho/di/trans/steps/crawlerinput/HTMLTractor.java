package org.pentaho.di.trans.steps.crawlerinput;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.pentaho.di.trans.steps.crawlerinput.cookie.Cookie;
import org.pentaho.di.trans.steps.crawlerinput.cookie.CookieException;
import org.pentaho.di.trans.steps.crawlerinput.cookie.MemoryCookieManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import org.apache.log4j.Category;

import net.matuschek.http.HttpConstants;
import net.matuschek.http.HttpException;
import net.matuschek.http.HttpHeader;
import net.matuschek.util.Base64;
import net.matuschek.util.ByteBuffer;
import net.matuschek.util.LimitedBandwidthStream;

/**
 * <p>Title: </p>
 *
 * <p>Description: get the content of a html page</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class HTMLTractor {

    URL url = null;

    URLConnection urlconn = null;
    Properties prop = null;

    /** Carriage return */
    final static byte CR = 13;

    /** Line feed */
    final static byte LF = 10;

    /** used HTTP version */
    final static String HTTP_VERSION = "HTTP/1.1";

    /* Status constants */

    /** HTTP connection will be established */
    public final static int STATUS_CONNECTING = 0;
    /** HTTP connection was established, but no data where retrieved */
    public final static int STATUS_CONNECTED = 1;
    /** data will be retrieved now */
    public final static int STATUS_RETRIEVING = 2;
    /** download finished */
    public final static int STATUS_DONE = 3;
    /** download could not be finished because a DownloadRule denied it */
    public final static int STATUS_DENIEDBYRULE = 4;

    /** default HTTP port */
    private final static int DEFAULT_HTTPPORT = 80;

    /** default agent name */
    private final static String AGENTNAME = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; GTB6.4; .NET CLR 2.0.50727) ";

    /**
     * default update interval for calls of the callback interfaces (in bytes)
     */
    private final static int DEFAULT_UPDATEINTERVAL = 10*1024;

    /** default socket timeout in seconds */
    private final static int DEFAULT_SOCKETTIMEOUT = 60;

    private static final int MAX_DOC_SIZE = 300*1024*1024;  //retrive max 300 M

    /** HTTP AgentName header */
    private String agentName = AGENTNAME;

    /** HTTP From header */
    private String fromAddress = null;

    /** Date of the HTTP If-Modified-Since header */
    private Date modifyDate = null;

    /**
     * maximal used bandwidth in bytes per second 0 disables bandwidth
     * limitations
     */
    private int bandwidth = 0;

    /** proxy address */
    private InetAddress proxyAddr = null;

    /** proxy port number */
    private int proxyPort = 0;

    /** textual description of the proxy (format host:port) */
    private String proxyDescr = "";

    /** timeout for getting data in seconds */
    private int socketTimeout = DEFAULT_SOCKETTIMEOUT;

    /** HttpTool should accept and use cookies */
    private boolean cookiesEnabled = false;

    /** Log4J Category object for logging */
    private Category log = null;

    private MemoryCookieManager cookieManager = null;

    public HTMLTractor(URL u,boolean cookiesEnabled) {
        this.url = u;
        log = Category.getInstance(getClass().getName());
        this.cookiesEnabled = cookiesEnabled;
        if(cookiesEnabled)
            cookieManager = new MemoryCookieManager();
    }

    /**
     * @param prop Properties :用于post 方式的提交,记录连接的属性
     * @param urlString String
     */
    public HTMLTractor(String urlString, Properties prop,boolean cookiesEnabled) {
        try {
            url = new URL(urlString);
            this.cookiesEnabled = cookiesEnabled;
            if(cookiesEnabled)
                cookieManager = new MemoryCookieManager();
            log = Category.getInstance(getClass().getName());
        }
        catch (MalformedURLException ex) {
            debug(url);
            ex.printStackTrace();
        }
        catch (Exception ex) {
            debug(url);
            ex.printStackTrace();
        }
        try {
            urlconn = url.openConnection();
        }
        catch (IOException ex1) {
            ex1.printStackTrace();
        }
        this.prop = prop;
    }

    /**
     * construct the tractor using a url string
     * such as "http://www.nicholaschase.com/testsoap.php"
     * @param urlString String
     */
    public HTMLTractor(String urlString) throws MalformedURLException,
            IOException {
        url = new URL(urlString);
        urlconn = url.openConnection();
    }

    /**
     *
     * @param defaultEncoding, if can't find the encoding from http response header.
     * @return
     * @throws IOException
     */
    public String getEncoding(String defaultEncoding) throws IOException  {
        String encoding  = urlconn.getContentEncoding();
        if(encoding == null)
            encoding = defaultEncoding;
        return encoding;
    }

    public String getSource(String encoding) throws IOException, Exception {
        InputStream is = null;
        StringWriter writer = new StringWriter();
        InputStreamReader reader = null;
        urlconn.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        urlconn.setRequestProperty("cookie", "ckP230009=1; JSESSIONID-B2CACC=sz10DNHsHSHQv1RNzpaNYV4LbIfrR0Gmf97QVgfXRnJlOAjhCP01!-1489378825!1669909271178; WMONID=GBn_UgGMnTo; XTVID=A2212020039200176; JSESSIONID=sz10DNHsHSHQv1RNzpaNYV4LbIfrR0Gmf97QVgfXRnJlOAjhCP01!-1489378825; HMF_CI=cdc9f6a1db05215a421d79728ee5f7e0d655d9863d221ce8fcc580b163013439878ba86c45382d02c92476ea123ed8572bd9e7c4fbad0c92345d387931b089015d; HBB_HC=9b96ed6eda03566a0dd7227ace959732214343b695e02462b6f12200aa758f0d440e664d1f0d9228ae26863afa4845568d; _harry_ref=; _harry_url=https://www.shilladfs.com/estore/kr/zh/p/230009; _harry_fid=hh534188664; _harry_hsid=A221201233922813374; _harry_dsid=A221201233922814589; XTSID=A221201233922815971; xloc=1440X900; _harry_lang=en; Hm_lvt_a775346088d985c129acc5f2383ec066=1669909164; Hm_lpvt_a775346088d985c129acc5f2383ec066=1669909164; keywordsort=true; keywordRandIndex=0; _BS_GUUID=YcWDu0o6sR0Rwa9Ycj8z5KwEtIO1I6aHj7kO7JUz; _TRK_AUIDA_13317=5d6d8950e0c029f737ccab07605c9026:1; _TRK_ASID_13317=597e984304009e4ed04e8e45f1d13fcd");
        urlconn.setRequestProperty("User-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
        urlconn.setRequestProperty("scheme", "https");
        urlconn.setRequestProperty("method", "GET");
        urlconn.setRequestProperty("accept-language", "en,zh-CN;q=0.9,zh;q=0.8,en-CN;q=0.7");
        //urlconn.setRequestProperty("accept-encoding", "gzip, deflate, br");
        urlconn.setRequestProperty("path", "/estore/kr/zh/p/230009");
        urlconn.setRequestProperty("content-encoding", "gzip");
        urlconn.setRequestProperty("content-language", "en,zh");
        urlconn.setRequestProperty("content-type", "text/html;charset=UTF-8");
        is = urlconn.getInputStream();
        if(encoding ==null)
            encoding  = this.getEncoding("utf-8");
        reader = new InputStreamReader(is,encoding);

        if (reader == null) {
            return null;
        }
        char[] buffer = new char[1024];
        int leng = 0;
        leng = reader.read(buffer, 0, 1024);
        while (leng != -1) {
            writer.write(buffer, 0, leng);
            leng = reader.read(buffer, 0, 1024);
        }
        return writer.toString();
    }

    /**
     * get html content through post style.
     * param content : content of http request by post style
     * content example : "_pageSize=40&_pageIndex=" + no + "&q_type=lease&q_company_id=0&q_agent_id=0&q_keyword=0&q_area_num=0&q_district_num=0&q_price_cur=0&q_uniformId=0&q_login_id=0&q";
     */

    public String getSourceFromPost(String content) throws IOException {
        try {
            urlconn = url.openConnection();
        }
        catch (IOException ex) {
            throw ex;
        }

        DataOutputStream printout = null;
        InputStream is = null;
        StringWriter writer = new StringWriter();
        if (!urlconn.getDoInput()) {
            urlconn.setDoInput(true);
            // Let the RTS know that we want to do output.
        }
        if (!urlconn.getDoOutput()) {
            urlconn.setDoOutput(true);
            // No caching, we want the real thing.
        }
        if (!urlconn.getUseCaches()) {
            urlconn.setUseCaches(false);
        }

        Iterator it = prop.keySet().iterator();
        String key = null;
        while (it.hasNext()) {
            key = (String) it.next();
            urlconn.setRequestProperty(key,
                    prop.getProperty(key));
        }
        try {
            printout = new DataOutputStream(urlconn.getOutputStream());
            printout.writeBytes(content);
            printout.flush();
            printout.close();

        }
        catch (IOException ex) {
            throw ex;
        }

        InputStreamReader reader = null;
        try {
            is = urlconn.getInputStream();
            reader = new InputStreamReader(is);
        }
        catch (IOException ex) {
            throw ex;
        }
        if (reader == null) {
            return null;
        }
        char[] buffer = new char[1024];
        int leng = 0;

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException ex1) {
        }

        try {
            leng = reader.read(buffer, 0, 1024);
            while (leng != -1) {
                writer.write(buffer, 0, leng);
                leng = reader.read(buffer, 0, 1024);
            }

        }
        catch (IOException ex) {
            throw ex;
        }
        return writer.toString();
    }

    /**
     * a simple get bytes method for extract content from a url
     * @return
     * @throws IOException
     * @throws Exception
     */
    public byte[] getBytes() throws IOException, Exception {
        InputStream is = null;
        ByteBuffer byteBuffer = new ByteBuffer();
        if (urlconn == null)
            getConnection();
        is = urlconn.getInputStream();
        int b;
        while ((b=is.read())>-1) {
            byteBuffer.append((byte)b);
        }
        return byteBuffer.getContent();
    }




    /**
     * Retrieves a byte array from the given URL. If Cookies are enabled it will
     * use the CookieManager to set Cookies it got from former retrieveDocument
     * operations.
     *
     * @param referer the URL to retrieve (only http:// supported yet)
     * @param method
     *            HttpConstants.GET for a GET request, HttpConstants.POST for a
     *            POST request
     * @param parameters
     *            additional parameters. Will be added to the URL if this is a
     *            GET request, posted if it is a POST request
     *
     * @see HttpConstants
     */
    public byte[] getBytes(String referer, int method, String parameters,boolean readWhenZero)
            throws HttpException {
        String host = null;
        InetAddress addr = null;
        String path = null;
        String requestPath = null;
        String protocol = null;
        String userinfo = null;
        boolean chunkedEncoding = false;
        ChunkedInputStream chunkStream = null;
        Hashtable<Object, Object> headerMap = new Hashtable<Object, Object> ();

        int port = 0;
        int i = 0;

        // document buffer
        ByteBuffer buff = new ByteBuffer();

        // the connection to the HTTP server
        HttpConnection httpConn = null;

        InputStream is = null;
        BufferedWriter bwrite = null;

        // get host
        host = url.getHost();
        if (host == null) {
            throw new HttpException("no host part in URL found");
        }

        // get address
        try {
            addr = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            addr = null;
        }
        if (addr == null) {
            throw new HttpException("host part (" + host + ") does not resolve");
        }

        // get path
        path = url.getFile();
        if (path.equals("")) {
            path = "/";
        }

        // if using the proxy, request path is the whole URL, otherwise only
        // the path part of the URL
        if (useProxy()) {
            requestPath = "http://" + host + path;
        } else {
            requestPath = path;
        }

        // get user info
        userinfo = url.getUserInfo();
        if ((userinfo != null) && userinfo.equals("")) {
            userinfo = null;
        }

        // get protocol and port
        port = url.getPort();
        protocol = url.getProtocol().toLowerCase();
        if (protocol.equals("http")) {
            if (port == -1) {
                port = DEFAULT_HTTPPORT;
            }
        } else if (protocol.equals("ftp")) // jasonchu 20090810
        {

        } else {
            throw new HttpException("protocol " + protocol + " not supported");
        }

        Object doc;
        // okay, we got all needed information, try to connect to the host
        try {
            // connect and initialize streams
            // timeout is stored in seconds in HttpTool, but
            // HttpConnection uses milliseconds
            if (useProxy()) {
                httpConn = HttpConnection.createConnection(proxyAddr,
                        proxyPort, socketTimeout * 1000);
            } else {
                httpConn = HttpConnection.createConnection(addr, port,
                        socketTimeout * 1000);
            }

            is = new LimitedBandwidthStream(new BufferedInputStream(httpConn
                    .getInputStream(), 256), bandwidth);
            bwrite = new BufferedWriter(new OutputStreamWriter(httpConn
                    .getOutputStream()));
            // write HTTP request
            if (method == HttpConstants.GET) {
                bwrite.write("GET ");
                bwrite.write(requestPath);
                if ((parameters != null) && (!parameters.equals(""))) {
                    bwrite.write("?");
                    bwrite.write(parameters);
                }

            } else if (method == HttpConstants.POST) {
                bwrite.write("POST " + requestPath);
            } else {
                throw new HttpException("HTTP method " + method
                        + " not supported");
            }

            // last part of request line
            bwrite.write(" ");
            bwrite.write(HTTP_VERSION);
            bwrite.write("\r\n");
            bwrite.write("Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, application/x-silverlight, */*\r\n");
            // Referer header only if defined
            if (referer != null) {
                bwrite.write("Referer: " + referer + "\r\n");
            }
            bwrite.write("Accept-Language: zh-cn"+ "\r\n");
            bwrite.write("User-Agent: Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727"+ "\r\n");
            bwrite.write("Content-Type: application/x-www-form-urlencoded"+ "\r\n");
            //bwrite.write("Accept-Encoding: gzip, deflate"+ "\r\n");

            // if cookies are enabled, write a Cookie: header
//			if (cookiesEnabled) {
//				String cookieString = cookieManager.cookiesForURL(url);
//				if (cookieString != null) {
//					bwrite.write("Cookie: ");
//					bwrite.write(cookieString);
//					bwrite.write("\r\n");
//					log.debug("Cookie request header: " + cookieString);
//				}
//			}

            // Write other headers
            bwrite.write("Host: " + host + "\r\n");
            if (method == HttpConstants.POST) {
                bwrite.write("Content-Length: " + parameters.length() + "\r\n");
            }
            // bwrite.write("Connection: close\r\n");
            bwrite.write("Connection: Keep-Alive\r\n");
            bwrite.write("Cache-Control: no-cache\r\n");
            bwrite.write("Cookie: __utma=143215594.1873952579.1323488527.1323488527.1323488527.1; __utmb=143215594.1.10.1323488527; __utmc=143215594; __utmz=143215594.1323488527.1.1.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=%E5%AE%BF%E9%9B%BE%E8%88%AA%E7%A9%BA; ASP.NET_SessionId=s02udl45kamixm55gjguqg55"+ "\r\n");

            // Write "From:" header only if a fromAddress is defined
            if (fromAddress != null) {
                bwrite.write("From: " + fromAddress + "\r\n");
            }

            // if we have username and password, lets write an Authorization
            // header
            if (userinfo != null) {
                bwrite.write("Authorization: Basic ");
                bwrite.write(Base64.encode(userinfo));
                bwrite.write("\r\n");
            }

            // if there is a "If-Modified-Since" date, also write this header
            if (modifyDate != null) {
                SimpleDateFormat df = new SimpleDateFormat(
                        "EEE, dd MMM yyyy hh:mm:ss z");
                String dateStr = df.format(modifyDate);

                bwrite.write("If-Modified-Since: ");
                bwrite.write(dateStr);
                bwrite.write("\r\n");
                log.debug("If-Modified-Since header: " + dateStr);
            }

            // for a POST request we also need a content-length header
            bwrite.write("\r\n");
            // finished headers
            // if this is a POST request, we have to add the POST parameters
            if (method == HttpConstants.POST) {
                bwrite.write(parameters);

            }
            bwrite.flush();

            // read the first line (HTTP return code)
            while ((i = is.read()) != 10) {
                if (i == -1) {
                    throw new HttpException("Could not get HTTP return code "
                            + "(buffer content is " + buff.toString() + ")");
                }
                buff.append((byte) i);
            }

            String httpCode = lineString(buff.getContent());
            buff.clean();

            if(httpCode.indexOf("200")<0 && httpCode.indexOf("302")<0)
                return null;
            // read the HTTP headers
            boolean finishedHeaders = false;
            while (!finishedHeaders) {
                i = is.read();
                if (i == -1) {
                    throw new HttpException("Could read HTTP headers");
                }
                if (i >= 32) {
                    buff.append((byte) i);
                }
                // HTTP header processing
                if (i == LF) {
                    String line = lineString(buff.getContent());

                    buff.clean();
                    // empty line means "end of headers"
                    if (line.trim().equals("")) {
                        finishedHeaders = true;
                    } else {
                        HttpHeader head = new HttpHeader(line);

                        headerMap.put(head.getName(),head.getValue());
                        if (cookiesEnabled && head.isSetCookie()) {
                            try {
                                Cookie cookie = new Cookie(head.toLine(), url);
                                cookieManager.add(cookie);
                                log.debug("Got a cookie " + cookie);
                            } catch (CookieException e) {
                                log.info("Could not interpret cookie: "
                                        + e.getMessage());
                            }
                        }

                        // Content chunked ?
                        if (head.getName()
                                .equalsIgnoreCase("Transfer-Encoding")
                                && head.getValue().equalsIgnoreCase("chunked")) {
                            chunkedEncoding = true;
                        }

                    }
                }
            }
            buff.clean();


            // if we got encoding "chunked", use the ChunkedInputStream
            if (chunkedEncoding) {
                chunkStream = new ChunkedInputStream(is);
            }

            // did we got an Content-Length header ?

            int docSize;
            try{
                docSize = new Integer(headerMap.get("Content-Length").toString()).intValue();
            }catch(Exception e)
            {
                docSize = -1;
            }

            if (docSize > 0) {
                buff.setSize(docSize);
            }


            FileOutputStream fos = null;


            // read data
            boolean finished = false;

            if(docSize == 0 && !readWhenZero)// don't read when doc size is zero,to get better performance.
                finished = true;

            int count = 0;

            while (!finished) {
                if (chunkedEncoding) {
                    i = chunkStream.read();
                } else {
                    i = is.read();
                }

                if (i == -1) {
                    // this should only happen on HTTP/1.0 responses
                    // without a Content-Length header
                    finished = true;
                } else {
                    if (fos!= null)  // it's a big file
                        fos.write(i);
                    else
                        buff.append((byte) i);
                    count++;
                }

                // if there was a Content-Length header stop after reading the
                // given number of bytes
                if (count == docSize) {
                    finished = true;
                }

                // if it is a chunked stream we should use the isDone method
                // to look if we reached the end
                if (chunkedEncoding) {
                    if (chunkStream.isDone()) {
                        finished = true;
                    }
                }

            }

            if (fos!= null)  // already write to a file, return a document with null.
            {
                fos.flush();
                return null;
            }


            httpConn.close();


        } catch (IOException e) {
            throw new HttpException(e.getMessage());
        }

        return buff.getContent();
    }
    private boolean useProxy() {
        // TODO Auto-generated method stub
        return false;
    }

    private URLConnection getConnection()
    {
        if (this.urlconn != null)
            return this.urlconn;
        else{
//		SocketAddress addr = new InetSocketAddress("10.99.60.201",8080);//是代理地址:192.9.208.16:3128
//		Proxy typeProxy = new Proxy(Proxy.Type.HTTP, addr);
            try {
//	        urlconn = url.openConnection(typeProxy);
                urlconn = url.openConnection();
            }
            catch (IOException ex1) {
                ex1.printStackTrace();
            }
        }
        return this.urlconn;

    }
    private boolean isDebug = true;
    public void debug(Object o) {
        if (o == null) {
            System.out.println("in class HTMLTractor object: " + "is null");
            return;
        }
    }


    protected String lineString(char[] b) {
        if (b.length == 0) {
            return "";
        }

        if (b[b.length - 1] != CR) {
            return new String(b);
        } else {
            return new String(b, 0, b.length - 1);
        }
    }

    /**
     * convert an array of bytes to a String. if the last byte is an CR it will
     * be ignored
     */
    protected String lineString(byte[] b) {
        if (b.length == 0) {
            return "";
        }

        if (b[b.length - 1] != CR) {
            return new String(b);
        } else {
            return new String(b, 0, b.length - 1);
        }
    }
}
