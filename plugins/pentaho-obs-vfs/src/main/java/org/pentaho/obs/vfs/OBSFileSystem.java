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

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obs.services.ObsClient;

/**
 * 
 * 
 * @author Sun
 * @since 2019年8月21日
 * @version
 * 
 */
public class OBSFileSystem extends AbstractFileSystem implements FileSystem {

  private static final Logger logger = LoggerFactory.getLogger(OBSFileSystem.class);

  // private ObsClient client;

  // An idle client
  private final AtomicReference<ObsClient> idleClient = new AtomicReference<ObsClient>();

  public OBSFileSystem(final FileName name, final ObsClient client, final FileSystemOptions fileSystemOptions) {
    super(name, null, fileSystemOptions);

    // this.client = client;
    this.idleClient.set(client);
  }

  @Override
  protected void addCapabilities(final Collection<Capability> caps) {
    caps.addAll(OBSFileProvider.capabilities);
  }

  protected ObsClient getClient() {
    ObsClient client = idleClient.getAndSet(null);

    if (client == null) {

      UserAuthenticationData authData = null;
      try {
        OBSFileName rootName = (OBSFileName) getRoot().getName();

        authData = UserAuthenticatorUtils.authenticate(getFileSystemOptions(), OBSFileProvider.AUTHENTICATOR_TYPES);

        client = OBSClientFactory.createConnection(rootName.getScheme(), rootName.getEndPoint(),
            UserAuthenticatorUtils
                .toString(UserAuthenticatorUtils.getData(authData, UserAuthenticationData.USERNAME, UserAuthenticatorUtils.toChar(rootName.getAccessKey()))),
            UserAuthenticatorUtils
                .toString(UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD, UserAuthenticatorUtils.toChar(rootName.getSecretKey()))),
            getFileSystemOptions());

      } catch (FileSystemException e) {
        e.printStackTrace();
      } finally {
        UserAuthenticatorUtils.cleanup(authData);
      }

    }

    return client;
  }

  @Override
  protected void doCloseCommunicationLink() {
    if (getClient() != null) {
      try {
        getClient().close();
      } catch (IOException e) {
        logger.warn("vfs.provider.ftp/close-connection.error", e);
      }
    }
  }

  public void putClient(final ObsClient client) {
    // Save client for reuse if none is idle.
    if (!idleClient.compareAndSet(null, client)) {
      // An idle client is already present so close the connection.
      try {
        client.close();
      } catch (IOException e) {
        logger.warn("vfs.provider.ftp/close-connection.error", e);
      }
    }
  }

  protected FileObject createFile(final AbstractFileName name) throws Exception {
    return new OBSFileObject(name, this);
  }

}
