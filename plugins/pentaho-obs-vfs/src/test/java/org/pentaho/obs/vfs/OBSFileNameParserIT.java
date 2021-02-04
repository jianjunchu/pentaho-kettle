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

import java.util.Properties;

import org.apache.commons.vfs2.provider.FileNameParser;
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
public class OBSFileNameParserIT {

  public static String awsAccessKey;
  public static String awsSecretKey;

  public static final String HOST = "obs";
  public static final String SCHEME = "obs";
  public static final int PORT = 843;

  @BeforeClass
  public static void init() throws Exception {
    Properties settings = new Properties();
    settings.load(OBSFileNameParserIT.class.getResourceAsStream("/test-settings.properties"));
    awsAccessKey = settings.getProperty("obsAccessKey");
    awsSecretKey = settings.getProperty("obsSecretKey");
  }

  @Test
  public void testParseUri_withKeys() throws Exception {
    FileNameParser parser = OBSFileNameParser.getInstance();
    String origUri = "obs:///fooBucket/rcf-emr-staging";
    OBSFileName filename = (OBSFileName) parser.parseUri(null, null, origUri);

    assertEquals("fooBucket", filename.getBucketName());

    assertEquals(origUri, filename.getURI());
  }

}
