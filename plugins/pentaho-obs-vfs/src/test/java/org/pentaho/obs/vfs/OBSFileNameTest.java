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

import java.io.UnsupportedEncodingException;

import org.apache.commons.vfs2.FileType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * 
 * @author Sun
 * @since 2019年8月23日
 * @version
 * 
 */
public class OBSFileNameTest {

  private OBSFileName fileName = null;

  public static final String endPoint = "obs.cn-north-1.myhuaweicloud.com";
  public static final String accessKey = "RW4X6LNKYMBSQTCQMHKP";
  public static final String secretKey = "3lJYCCLvkPf7ZnGjq59oqOaSLGpJ9fYDxZH6N9TJ";

  public static final String HOST = "OBS";
  public static final String SCHEME = "obs";
  public static final int PORT = 843;

  @BeforeClass
  public static void init() throws Exception {
  }

  @Before
  public void setup() {
    fileName = new OBSFileName(SCHEME, "/", "", null, null, null, FileType.FOLDER);
  }

  @Test
  public void testGetURI() throws Exception {
    String expected = buildObsURL("/");
    assertEquals(expected, fileName.getURI());
  }

  @Test
  public void testCreateName() throws Exception {
    assertEquals("s3:///path/to/my/file", fileName.createName("/path/to/my/file", FileType.FILE).getURI());
  }

  @Test
  public void testAppendRootUriWithNonDefaultPort() throws Exception {
    fileName = new OBSFileName(SCHEME, "/", "FooFolder", null, null, null, FileType.FOLDER);
    String expectedUri = SCHEME + "://" + "FooFolder";
    assertEquals(expectedUri, fileName.getURI());

    fileName = new OBSFileName(SCHEME, "FooBucket", "FooBucket/FooFolder", expectedUri, expectedUri, expectedUri, FileType.FOLDER);
    expectedUri = SCHEME + "://FooBucket/" + "FooFolder";
    assertEquals(expectedUri, fileName.getURI());
  }

  public static String buildObsURL(String path) throws UnsupportedEncodingException {
    return SCHEME + "://" + path;
  }

}
