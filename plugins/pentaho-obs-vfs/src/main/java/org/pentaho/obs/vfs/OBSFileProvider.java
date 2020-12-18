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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

import com.obs.services.ObsClient;

/**
 * 
 * 
 * @author Sun
 * @since 2019年8月21日
 * @version
 * 
 */
public class OBSFileProvider extends AbstractOriginatingFileProvider {

  /**
   * The scheme this provider was designed to support
   */
  public static final String SCHEME = "obs";

  /**
   * User Information.
   */
  public static final String ATTR_USER_INFO = "UI";

  /**
   * Authentication types.
   */
  public static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES = new UserAuthenticationData.Type[] {
      /** The user name. */
      UserAuthenticationData.USERNAME,
      /** The password. */
      UserAuthenticationData.PASSWORD };

  /**
   * The provider's capabilities.
   */
  public static final Collection<Capability> capabilities = Collections.unmodifiableCollection(Arrays.asList(new Capability[] {
      /** CREATE. */
      Capability.CREATE,
      /** DELETE. */
      Capability.DELETE,
      /** RENAME. */
      Capability.RENAME,
      /** GET_TYPE. */
      Capability.GET_TYPE,
      /** LIST_CHILDREN. */
      Capability.LIST_CHILDREN,
      /** READ_CONTENT. */
      Capability.READ_CONTENT,
      /** URI. */
      Capability.URI,
      /** WRITE_CONTENT. */
      Capability.WRITE_CONTENT,
      /** GET_LAST_MODIFIED. */
      Capability.GET_LAST_MODIFIED,
      /** RANDOM_ACCESS_READ. */
      Capability.RANDOM_ACCESS_READ }));

  public OBSFileProvider() {
    super();
    setFileNameParser(OBSFileNameParser.getInstance());
  }

  @Override
  protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions) throws FileSystemException {
    // Create the file system
    final OBSFileName rootName = (OBSFileName) name;

    UserAuthenticationData authData = null;
    ObsClient client = null;
    try {
      authData = UserAuthenticatorUtils.authenticate(fileSystemOptions, AUTHENTICATOR_TYPES);

      client = OBSClientFactory.createConnection(rootName.getScheme(), rootName.getEndPoint(),
          UserAuthenticatorUtils
              .toString(UserAuthenticatorUtils.getData(authData, UserAuthenticationData.USERNAME, UserAuthenticatorUtils.toChar(rootName.getAccessKey()))),
          UserAuthenticatorUtils
              .toString(UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD, UserAuthenticatorUtils.toChar(rootName.getSecretKey()))),
          fileSystemOptions);
    } finally {
      UserAuthenticatorUtils.cleanup(authData);
    }

    return new OBSFileSystem(name, client, fileSystemOptions);
  }

  @Override
  public FileSystemConfigBuilder getConfigBuilder() {
    return OBSFileSystemConfigBuilder.getInstance();
  }

  @Override
  public Collection<Capability> getCapabilities() {
    return capabilities;
  }

}
