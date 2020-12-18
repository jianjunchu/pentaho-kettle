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
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.UriParser;

/**
 * 
 * 
 * @author Sun
 * @since 2019年8月21日
 * @version
 * 
 */
public class OBSFileName extends AbstractFileName {

  private static final char[] ACCESS_KEY_RESERVED = { ':', '@', '/' };
  private static final char[] SECRET_KEY_RESERVED = { '@', '/', '?' };

  public static final String DELIMITER = "/";

  private String endPoint;

  private String accessKey;

  private String secretKey;

  private String bucketName;

  private String objectKeye;

  private String bucketRelativePath;

  public OBSFileName(final String scheme, final String endPoint, final String accessKey, final String secretKey, final String bucketName, final String absPath,
      final FileType type) {
    super(scheme, absPath, type);

    this.endPoint = endPoint;
    this.accessKey = accessKey;
    this.secretKey = secretKey;

    this.bucketName = bucketName;
    this.objectKeye = "";

    if (absPath.length() > 1) {
      this.bucketRelativePath = absPath.substring(1);
      if (type.equals(FileType.FOLDER)) {
        this.bucketRelativePath += DELIMITER;
      }
    } else {
      this.bucketRelativePath = "";
    }
  }

  @Override
  public String getURI() {
    final StringBuilder buffer = new StringBuilder();
    appendRootUri(buffer, true);
    buffer.append(getPath());
    return buffer.toString();
  }

  @Override
  public FileName createName(final String absPath, final FileType type) {
    return new OBSFileName(getScheme(), endPoint, accessKey, secretKey, bucketName, absPath, type);
  }

  @Override
  protected void appendRootUri(final StringBuilder buffer, final boolean addPassword) {
    buffer.append(getScheme());
    buffer.append("://");
    appendCredentials(buffer, addPassword);
    buffer.append(endPoint);

    // if (bucketName != null && bucketName.length() > 0) {
    // buffer.append(DELIMITER);
    // buffer.append(bucketName);
    // }
  }

  protected void appendCredentials(final StringBuilder buffer, final boolean addPassword) {
    if (accessKey != null && accessKey.length() != 0) {
      UriParser.appendEncoded(buffer, accessKey, ACCESS_KEY_RESERVED);
      if (secretKey != null && secretKey.length() != 0) {
        buffer.append(':');
        if (addPassword) {
          UriParser.appendEncoded(buffer, secretKey, SECRET_KEY_RESERVED);
        } else {
          buffer.append("***");
        }
      }
      buffer.append('@');
    }
  }

  public String getEndPoint() {
    return endPoint;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public String getBucketName() {
    return bucketName;
  }

  public String getObjectKey() {
    return objectKeye;
  }

  public String getBucketRelativePath() {
    return bucketRelativePath;
  }

}
