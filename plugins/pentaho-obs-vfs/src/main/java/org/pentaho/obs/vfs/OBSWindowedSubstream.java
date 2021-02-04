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

import java.io.ByteArrayInputStream;

/**
 * 
 * 
 * @author Sun
 * @since 2019年8月26日
 * @version
 * 
 */
public class OBSWindowedSubstream extends ByteArrayInputStream {

  public OBSWindowedSubstream(byte[] buf) {
    super(buf);
  }

  @Override
  public synchronized long skip(long n) {
    // virtual skip
    return n;
  }

}
