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

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

/**
 * 
 * 
 * @author Sun
 * @since 2019年8月23日
 * @version
 * 
 */
public final class OBSFileSystemConfigBuilder extends FileSystemConfigBuilder {

  private static final OBSFileSystemConfigBuilder BUILDER = new OBSFileSystemConfigBuilder();

  private static final String ENDPOINT = "endPoint";

  private static final String ACCESS_KEY = "accessKey";

  private static final String SECRET_KEY = "secretKey";

  private OBSFileSystemConfigBuilder() {
    super("obs.");
  }

  public static OBSFileSystemConfigBuilder getInstance() {
    return BUILDER;
  }

  @Override
  protected Class<? extends FileSystem> getConfigClass() {
    return OBSFileSystem.class;
  }

  public String getEndPoint(final FileSystemOptions opts) {
    return this.getString(opts, ENDPOINT);
  }

  public void setEndPoint(final FileSystemOptions opts, final String endPoint) {
    this.setParam(opts, ENDPOINT, endPoint);
  }

  public String getAccessKey(final FileSystemOptions opts) {
    return this.getString(opts, ACCESS_KEY);
  }

  public void setAccessKey(final FileSystemOptions opts, final String accessKey) {
    this.setParam(opts, ACCESS_KEY, accessKey);
  }

  public String getSecretKey(final FileSystemOptions opts) {
    return this.getString(opts, SECRET_KEY);
  }

  public void setSecretKey(final FileSystemOptions opts, final String secretKey) {
    this.setParam(opts, SECRET_KEY, secretKey);
  }

}
