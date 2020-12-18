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

package org.pentaho.obs;

import java.util.Arrays;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.obs.vfs.OBSFileProvider;

/**
 * 
 * 
 * @author Sun
 * @since 2019年8月22日
 * @version
 * 
 */
@KettleLifecyclePlugin(id = "OBSFileSystemBootstrap", name = "OBS FileSystem Bootstrap")
public class OBSFileSystemBootstrap implements KettleLifecycleListener {

  private static Class<?> PKG = OBSFileSystemBootstrap.class;

  private LogChannelInterface log = new LogChannel(OBSFileSystemBootstrap.class.getName());

  @Override
  public void onEnvironmentInit() throws LifecycleException {
    try {
      // Register OBS as a file system type with VFS
      FileSystemManager fsm = KettleVFS.getInstance().getFileSystemManager();
      if (fsm instanceof DefaultFileSystemManager) {
        if (!Arrays.asList(fsm.getSchemes()).contains(OBSFileProvider.SCHEME)) {
          ((DefaultFileSystemManager) fsm).addProvider(OBSFileProvider.SCHEME, new OBSFileProvider());
        }
      }
    } catch (FileSystemException e) {
      log.logError(BaseMessages.getString(PKG, "AmazonSpoonPlugin.StartupError.FailedToLoadS3Driver"));
    }
  }

  @Override
  public void onEnvironmentShutdown() {

  }

}
