/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.openapiclient;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.api.OpenApiClient;
import com.actionsoft.sdk.service.response.IntResponse;
import com.actionsoft.sdk.service.response.StringArrayResponse;
/**
 * Run open api .
 */
public class OpenAPIClient extends BaseStep implements StepInterface {
  private static Class<?> PKG = OpenAPIClient.class; // i18n

  private OpenAPIClientMeta meta;
  private OpenAPIClientData data;
  private OpenApiClient client;


  public OpenAPIClient( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }


  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (OpenAPIClientMeta) smi;
    data = (OpenAPIClientData) sdi;

    String result=null;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...
      if(data.arrayList.size()>0) {
        data.result.put("recordDatas", data.arrayList);
        StringArrayResponse res = client.exec(meta.getMethodName(), data.result, StringArrayResponse.class);
        result = res.getResult();
        data.totalBatchCount += data.batchCount;
        if ( log.isBasic() ) {
          logBasic(BaseMessages.getString(PKG, "OpenAPIClientDialog.Log.execResult") + result);
          logBasic("Total Finished: " + data.totalBatchCount);
        }
        Object[] outputRowData = r;
        outputRowData = RowDataUtil.resizeArray(r, getInputRowMeta().size() + 1);
        outputRowData[getInputRowMeta().size()] = result;
        putRow(data.outputRowMeta, outputRowData);
      }
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
      data.result = new HashMap<String, Object>();
      data.result.put("boName", meta.getBoName());
      data.result.put("uid", meta.getuid());
      data.result.put("bindId", meta.getbindId());
      data.arrayList = new ArrayList<Map>();
      client = new OpenApiClient(meta.getAPIServer(),meta.getAccessKey(),meta.getSecret());
    }

    if ( log.isRowLevel() ) {
      logRowlevel( BaseMessages.getString( PKG, "OpenAPIClientDialog.Log.ReadRow" )
        + getLinesRead() + " : " + getInputRowMeta().getString( r ) );
    }

    try {

      Map map = new HashMap();
      String[] fields = getInputRowMeta().getFieldNames();
      for(int i=0;i<getInputRowMeta().size();i++) {
        map.put(fields[i], r[i]);
      }
      data.arrayList.add(map);
      data.batchCount++;
      int batchNumberInt;
      try {
        if(Const.isEmpty(meta.getBatchNumber()))
          batchNumberInt = 0;
        else
          batchNumberInt = new Integer(meta.getBatchNumber()).intValue();
      }catch(Exception e)
      {
        batchNumberInt = 0;
      }
      if(batchNumberInt>0 && data.batchCount>=batchNumberInt) {
        data.result.put("recordDatas", data.arrayList);
        StringArrayResponse res = client.exec(meta.getMethodName(), data.result, StringArrayResponse.class);
        result = res.getResult();
        data.totalBatchCount += data.batchCount;
        logBasic(BaseMessages.getString(PKG, "OpenAPIClientDialog.Log.execResult") + result);
        logBasic("Total Finished: " + data.totalBatchCount);

        data.batchCount=0;
        data.arrayList = new ArrayList<Map>();
      }

      Object[] outputRowData = r;
      outputRowData = RowDataUtil.resizeArray( r, getInputRowMeta().size() + 1 );
      outputRowData[getInputRowMeta().size()] = result;
      putRow( data.outputRowMeta, outputRowData );

      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "OpenAPIClientDialog.Log.WriteRow" )
          + getLinesWritten() + " : " + getInputRowMeta().getString( r ) );
      }
      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "OpenAPIClientDialog.Log.LineNumber" ) + getLinesRead() );
        }
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "OpenAPIClientDialog.Log.ErrorInStep" ) + e.getMessage() );
      setErrors( 1 );
      stopAll();
      setOutputDone(); // signal end to receiver(s)
      return false;
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (OpenAPIClientMeta) smi;
    data = (OpenAPIClientData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.secret = Const.toLong( environmentSubstitute( meta.getSecret() ), 1000 );
      data.methodName = getTransMeta().findSlaveServer( environmentSubstitute( meta.getMethodName() ) );
      data.accessKey = environmentSubstitute( meta.getAccessKey() );
      data.value = -1;

      return true;
    }
    return false;
  }
}
