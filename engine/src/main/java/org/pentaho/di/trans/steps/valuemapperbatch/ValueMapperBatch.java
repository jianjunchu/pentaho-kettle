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

package org.pentaho.di.trans.steps.valuemapperbatch;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import java.util.Hashtable;

/**
 * Convert Values in a certain fields to other values
 *
 * @author Matt
 * @since 3-apr-2006
 */
public class ValueMapperBatch extends BaseStep implements StepInterface {
  private static Class<?> PKG = ValueMapperBatchMeta.class; // for i18n purposes, needed by Translator2!!

  private ValueMapperBatchMeta meta;
  private ValueMapperBatchData data;
  //private boolean nonMatchActivated = false;

  public ValueMapperBatch( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (ValueMapperBatchMeta) smi;
    data = (ValueMapperBatchData) sdi;

    // Get one row from one of the rowsets...
    //
    Object[] r = getRow();
    if ( r == null ) { // means: no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      data.previousMeta = getInputRowMeta().clone();
      data.outputMeta = data.previousMeta.clone();
      meta.getFields( data.outputMeta, getStepname(), null, null, this, repository, metaStore );

      data.indexOfField = new int[meta.getFields().length];
      data.hashtables= new Hashtable[meta.getFields().length];
      data.sourceValueMeta=new ValueMetaInterface[meta.getFields().length];
      data.outputValueMeta=new ValueMetaInterface[meta.getFields().length];

      for ( int i = 0; i < meta.getFields().length; i++ ) {
        // Check if this field was specified only one time
        for (int j = 0; j < meta.getFields().length; j++) {
          if (meta.getFields()[j].equals(meta.getFields()[i])) {
            if (j != i) {
              throw new KettleException(BaseMessages.getString(
                      PKG, "ValueMapperBatch.Log.FieldSpecifiedMoreThatOne", meta.getFields()[i], "" + i, "" + j));
            }
          }
        }

        data.indexOfField[i] = data.previousMeta.indexOfValue( environmentSubstitute( meta.getFields()[i] ) );
        data.hashtables[i] = getHashTable(meta.getValueMappers()[i]);
        if ( data.indexOfField[i] < 0 ) {
          throw new KettleStepException( BaseMessages.getString(
                  PKG, "ValueMapperBatch.Log.CouldNotFindFieldInRow", meta.getFields()[i] ) );
        }
      data.sourceValueMeta[i] = getInputRowMeta().getValueMeta( data.indexOfField[i]  );
      data.outputValueMeta[i] = data.outputMeta.getValueMeta( data.indexOfField[i]  );
//      if ( Utils.isEmpty( meta.getTargetField() ) ) {
//        data.outputValueMeta[i] = data.outputMeta.getValueMeta( data.indexOfField[i]  ); // Same field
//
//      } else {
//        data.outputValueMeta[i] = data.outputMeta.searchValueMeta( meta.getTargetField() ); // new field
//      }
      }
    }
    try {
      for ( int i = 0; i < data.indexOfField.length; i++ ) {
        Object sourceData = r[data.indexOfField[i]];
        String sourceStr = data.sourceValueMeta[i].getCompatibleString( sourceData );
        String targetStr  = null;
        // Null/Empty mapping to value...
        if ( Utils.isEmpty( sourceStr ) )  {
          targetStr = sourceStr; // that's all there is to it.
        } else {
          targetStr = data.hashtables[i].get( sourceStr );
            if ( targetStr == null ) {
              // If we do non matching and we don't have a match
              targetStr = meta.getDefaultValues()[i];
            }
          }
        //r[data.indexOfField[i]] = data.outputValueMeta[i].convertData(data.outputValueMeta[i],targetStr);
        r[data.indexOfField[i]] = targetStr;
      }
      putRow( data.outputMeta, r ); // copy row to output rowset(s);
    } catch ( KettleException e ) {
      e.printStackTrace();
      boolean sendToErrorRow = false;
      String errorMessage = null;

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "ValueMapperBatch.Log.ErrorInStep", e.getMessage() ) );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( data.outputMeta, r, 1, errorMessage, null, "ValueMapperBatch001" );
      }
    }
    return true;
  }

  private Hashtable<String, String> getHashTable(String valueMapper) {
    Hashtable table = new Hashtable();
    String[] values = valueMapper.split(";");
    // Add all source to target mappings in here...
    for (int i = 0; i < values.length; i++ ) {

      String src = values[i].substring(0,values[i].indexOf(":"));
      String tgt = values[i].substring(values[i].indexOf(":")+1);

      if ( !Utils.isEmpty( src ) && !Utils.isEmpty( tgt ) ) {
        table.put( src, tgt );
      } else {
        if ( Utils.isEmpty( tgt ) ) {
          // allow target to be set to null since 3.0
          table.put( src, "" );
        }
      }
    }
    return table;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ValueMapperBatchMeta) smi;
    data = (ValueMapperBatchData) sdi;

    super.dispose( smi, sdi );
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ValueMapperBatchMeta) smi;
    data = (ValueMapperBatchData) sdi;

    if ( super.init( smi, sdi ) ) {

      return true;
    }
    return false;
  }

}
