/*******************************************************************************
 *
 * 
 *
 * Copyright (C) 2011-2019 by Sun : http://www.kingbase.com.cn
 *
 *******************************************************************************
 *
 *
 *    Email : snj1314@163.com
 *
 *
 ******************************************************************************/

package org.pentaho.obs.vfs;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileNameParser;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.apache.commons.vfs2.util.Cryptor;
import org.apache.commons.vfs2.util.CryptorFactory;

/**
 * 
 * 
 * @author Sun
 * @since 2019年8月21日
 * @version
 * 
 */
public class OBSFileNameParser extends AbstractFileNameParser {

  private static final OBSFileNameParser INSTANCE = new OBSFileNameParser();

  public OBSFileNameParser() {
    super();
  }

  public static FileNameParser getInstance() {
    return INSTANCE;
  }

  @Override
  public FileName parseUri(final VfsComponentContext context, final FileName base, final String uri) throws FileSystemException {
    final StringBuilder name = new StringBuilder();

    // Extract the scheme
    String scheme = UriParser.extractScheme(uri, name);

    // Extract the scheme and authority parts
    final Authority auth = extractToPath(uri, name);

    // Decode and normalise the file name
    UriParser.canonicalizePath(name, 0, name.length(), this);
    UriParser.fixSeparators(name);
    final FileType fileType = UriParser.normalisePath(name);
    final String path = name.toString();

    // Extract bucket name
    final String bucketName = UriParser.extractFirstElement(name);

    return new OBSFileName(scheme, auth.endPoint, auth.accessKey, auth.secretKey, bucketName, path, fileType);
  }

  protected Authority extractToPath(final String uri, final StringBuilder name) throws FileSystemException {
    final Authority auth = new Authority();

    // Extract the scheme
    auth.scheme = UriParser.extractScheme(uri, name);

    // Expecting "//"
    if (name.length() < 2 || name.charAt(0) != '/' || name.charAt(1) != '/') {
      throw new FileSystemException("vfs.provider/missing-double-slashes.error", uri);
    }
    name.delete(0, 2);

    // Extract userinfo, and split into username and password
    final String userInfo = extractUserInfo(name);
    final String accessKey;
    final String secretKey;
    if (userInfo != null) {
      final int idx = userInfo.indexOf(':');
      if (idx == -1) {
        accessKey = userInfo;
        secretKey = null;
      } else {
        accessKey = userInfo.substring(0, idx);
        secretKey = userInfo.substring(idx + 1);
      }
    } else {
      accessKey = null;
      secretKey = null;
    }
    auth.accessKey = UriParser.decode(accessKey);
    auth.secretKey = UriParser.decode(secretKey);

    if (auth.secretKey != null && auth.secretKey.startsWith("{") && auth.secretKey.endsWith("}")) {
      try {
        final Cryptor cryptor = CryptorFactory.getCryptor();
        auth.secretKey = cryptor.decrypt(auth.secretKey.substring(1, auth.secretKey.length() - 1));
      } catch (final Exception ex) {
        throw new FileSystemException("Unable to decrypt password", ex);
      }
    }

    // Extract endPoint, and normalise (lowercase)
    final String endPoint = extractEndPoint(name);
    if (endPoint == null) {
      throw new FileSystemException("vfs.provider/missing-hostname.error", uri);
    }
    auth.endPoint = endPoint.toLowerCase();

    // Expecting '/' or empty name
    if (name.length() > 0 && name.charAt(0) != '/') {
      throw new FileSystemException("vfs.provider/missing-hostname-path-sep.error", uri);
    }

    return auth;
  }

  protected String extractUserInfo(final StringBuilder name) {
    final int maxlen = name.length();
    for (int pos = 0; pos < maxlen; pos++) {
      final char ch = name.charAt(pos);
      if (ch == '@') {
        // Found the end of the user info
        final String userInfo = name.substring(0, pos);
        name.delete(0, pos + 1);
        return userInfo;
      }
      if (ch == '/' || ch == '?') {
        // Not allowed in user info
        break;
      }
    }

    // Not found
    return null;
  }

  protected String extractEndPoint(final StringBuilder name) {
    final int maxlen = name.length();
    int pos = 0;
    for (; pos < maxlen; pos++) {
      final char ch = name.charAt(pos);
      if (ch == '/' || ch == ';' || ch == '?' || ch == ':' || ch == '@' || ch == '&' || ch == '=' || ch == '+' || ch == '$' || ch == ',') {
        break;
      }
    }
    if (pos == 0) {
      return null;
    }

    final String endPoint = name.substring(0, pos);
    name.delete(0, pos);
    return endPoint;
  }

  /**
   * Parsed authority info (scheme, endpoint, accessKey/secretKey).
   */
  protected static class Authority {

    private String scheme;
    private String endPoint;
    private String accessKey;
    private String secretKey;

    public String getScheme() {
      return scheme;
    }

    public void setScheme(String scheme) {
      this.scheme = scheme;
    }

    public String getEndPoint() {
      return endPoint;
    }

    public void setEndPoint(String endPoint) {
      this.endPoint = endPoint;
    }

    public String getAccessKey() {
      return accessKey;
    }

    public void setAccessKey(String accessKey) {
      this.accessKey = accessKey;
    }

    public String getSecretKey() {
      return secretKey;
    }

    public void setSecretKey(String secretKey) {
      this.secretKey = secretKey;
    }

  }

}
