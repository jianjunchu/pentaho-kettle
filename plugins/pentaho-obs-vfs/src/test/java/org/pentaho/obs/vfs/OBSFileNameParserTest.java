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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;
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
public class OBSFileNameParserTest {

  FileNameParser parser;

  @Before
  public void setUp() throws Exception {
    parser = OBSFileNameParser.getInstance();
  }

  @Test
  public void testParseUri() throws Exception {
    VfsComponentContext context = mock(VfsComponentContext.class);
    FileName fileName = mock(FileName.class);
    String uri = "obs://bucket/file";
    FileName noBaseFile = parser.parseUri(context, null, uri);
    assertNotNull(noBaseFile);
    assertEquals("bucket", ((OBSFileName) noBaseFile).getBucketName());
    FileName withBaseFile = parser.parseUri(context, fileName, uri);
    assertNotNull(withBaseFile);
    assertEquals("bucket", ((OBSFileName) withBaseFile).getBucketName());

    // assumption is that the whole URL is valid until it comes time to resolve to OBS objects
    uri = "obs://obs/bucket/file";
    withBaseFile = parser.parseUri(context, fileName, uri);
    assertEquals("obs", ((OBSFileName) withBaseFile).getBucketName());
  }

}
