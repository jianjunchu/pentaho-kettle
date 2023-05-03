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

package org.pentaho.di.trans.steps.obsoutput;

import com.obs.services.ObsClient;
import com.obs.services.model.ObsBucket;
import com.obs.services.model.ObsObject;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputData;

import java.io.OutputStream;
import java.io.ObjectOutputStream;

/**
 * 
 * 
 * @author Sun
 * @since 2019年2月25日
 * @version
 * 
 */
public class OBSOutputData extends TextFileOutputData implements StepDataInterface {

  /**
   * 
   */

  public ObsClient client;
  public ObsBucket bucket;
  public int maxLineSize;
  public ObsObject object;
  public ObjectOutputStream obsObjectOutputtStream;
  public OutputStream fos;

  public OBSOutputData() {
    super();
  }

}
