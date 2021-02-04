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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemOptions;
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
public class OBSFileProviderTest {

  OBSFileProvider provider;

  @Before
  public void setUp() throws Exception {
    provider = new OBSFileProvider();
  }

  @Test
  public void testDoCreateFileSystem() throws Exception {
    FileName fileName = mock(FileName.class);
    FileSystemOptions options = new FileSystemOptions();
    assertNotNull(provider.doCreateFileSystem(fileName, options));
  }

}
