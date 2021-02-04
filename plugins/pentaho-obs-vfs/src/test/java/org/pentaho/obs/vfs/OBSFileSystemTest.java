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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author Sun
 * @since 2019年8月23日
 * @version
 * 
 */
public class OBSFileSystemTest {

  OBSFileSystem fileSystem;
  OBSFileName fileName;

  @Before
  public void setUp() throws Exception {
    fileName = new OBSFileName(OBSFileNameTest.SCHEME, "/", "", null, null, null, FileType.FOLDER);
    fileSystem = new OBSFileSystem(fileName, null, new FileSystemOptions());
  }

  @Test
  public void testCreateFile() throws Exception {
    assertNotNull(fileSystem.createFile(new OBSFileName("obs", "bucketName", "/bucketName/key", null, null, null, FileType.FILE)));
  }

  @Test
  public void testGetObsClient() throws Exception {
    assertNotNull(fileSystem.getClient());

    FileSystemOptions options = new FileSystemOptions();
    UserAuthenticator authenticator = mock(UserAuthenticator.class);
    UserAuthenticationData authData = mock(UserAuthenticationData.class);
    when(authenticator.requestAuthentication(OBSFileProvider.AUTHENTICATOR_TYPES)).thenReturn(authData);
    when(authData.getData(UserAuthenticationData.USERNAME)).thenReturn("username".toCharArray());
    when(authData.getData(UserAuthenticationData.PASSWORD)).thenReturn("password".toCharArray());
    DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(options, authenticator);

    fileSystem = new OBSFileSystem(fileName, null, options);
    assertNotNull(fileSystem.getClient());
  }
}
