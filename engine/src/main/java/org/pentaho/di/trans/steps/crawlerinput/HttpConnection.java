package org.pentaho.di.trans.steps.crawlerinput;

/*********************************************
    Copyright (c) 2001 by Daniel Matuschek
*********************************************/


import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

import java.net.Socket;
import java.net.InetAddress;


/**
 * An HttpConnection object simply represents a TCP socket connection
 * between the client and an HTTP server.
 * 
 * @author Daniel Matuschek 
 * @version $Id $
 */
public class HttpConnection {

  /** the TCP socket to use */
  private Socket socket = null;
  

  /**
   * Create a new HttpConnection to the given host and port.
   *
   * @param address the IP address to connect to
   * @param port the port to connect to (usually 80 for HTTP)
   * @param timeout connection timeout in milliseconds. If the connection
   * could not be established after this time, an exception will
   * be thrown. This timeout will also be set as timeout for the
   * TCP connection (so timeout)
   * An timeout of 0 will be interpreted as an infinite timeout.
   * @return a new HttpConnection object
   * @exception IOException if the TCP socket connection could
   * not be established
   */
  public static HttpConnection createConnection(InetAddress address, 
						int port,
						int timeout)
    throws IOException 
  {
    HttpConnection connection = new HttpConnection();
    try {
      connection.socket = TimedSocket.getSocket(address, port, timeout);
      connection.socket.setSoTimeout(timeout);
    } catch (InterruptedIOException e) {
      throw new IOException("timeout during connect: "+e.getMessage());
    }
    return connection;
  }

  /**
   * Gets the InputStream of this connection. Don't close this stream,
   * but close the HttpConnection when finished.
   *
   * @return an InputStream or null if no connection is established
   * @exception IOException if an I/O error occurs when creating the stream
   */
  public InputStream getInputStream() throws IOException {
    if (socket == null) throw new IOException("not conected");
    return socket.getInputStream();
  }


  /**
   * Gets the OutputStream of this connection. Don't close this stream,
   * but close the HttpConnection when finished.
   *
   * @return an OutputStream or null if no connection is established
   * @exception IOException if an I/O error occurs when creating the stream
   */
  public OutputStream getOutputStream() throws IOException {
    if (socket == null) throw new IOException("not conected");
    return socket.getOutputStream();
  }


  /** 
   * Close this connection (including the streams)
   */
  public void close() {
    try { 
      socket.close();
    } catch (IOException e) {
      // do not throw an I/O error on close
    }
  }


  /**
   * Create a new HttpConnection
   */
  protected HttpConnection() {
  }
}