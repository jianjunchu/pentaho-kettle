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

import org.apache.commons.vfs2.FileSystemOptions;
import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;

/**
 * 
 * 
 * @author Sun
 * @since 2019年8月26日
 * @version
 * 
 */
public class OBSClientFactory {

  private OBSClientFactory() {

  }

  public static ObsClient createConnection(final String scheme, final String endPoint, final String accessKey, final String secretKey,
      final FileSystemOptions fileSystemOptions) {
    return createConnection(OBSFileSystemConfigBuilder.getInstance(), scheme, endPoint, accessKey, secretKey, fileSystemOptions);
  }

  public static ObsClient createConnection(final OBSFileSystemConfigBuilder builder, final String scheme, final String endPoint, final String accessKey,
      final String secretKey, final FileSystemOptions fileSystemOptions) {
    ObsClient client;

    client = new ObsClient(accessKey, secretKey, endPoint);

    final ObsConfiguration config = new ObsConfiguration();
    config.setEndPoint(endPoint);

    if (fileSystemOptions != null) {
      //
    }

    return client;
  }

}
