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

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import java.util.Hashtable;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class ValueMapperBatchData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface previousMeta;
  public RowMetaInterface outputMeta;

  public int[] indexOfField;

  public Hashtable<String, String>[] hashtables;

  public ValueMetaInterface stringMeta;
  public ValueMetaInterface[] outputValueMeta;
  public ValueMetaInterface[] sourceValueMeta;

  public ValueMapperBatchData() {
    super();

    hashtables = null;

    stringMeta = new ValueMetaString( "string" );
  }

}
