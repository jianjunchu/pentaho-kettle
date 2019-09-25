/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.randomrowgenerator;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Generates a number of random rows
 *
 * @author Jason
 * @since 4-apr-2019
 */
public class RandomRowGenerator extends BaseStep implements StepInterface {
  private static Class<?> PKG = RandomRowGeneratorMeta.class; // for i18n purposes, needed by Translator2!!

  private RandomRowGeneratorMeta meta;

  private RandomRowGeneratorData data;

  public RandomRowGenerator(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                            Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    meta = (RandomRowGeneratorMeta) getStepMeta().getStepMetaInterface();
    data = (RandomRowGeneratorData) stepDataInterface;


  }

  public static final RowMetaAndData buildRow(RandomRowGeneratorMeta meta, List<CheckResultInterface> remarks,
                                              String origin ) throws KettlePluginException {
    RowMetaInterface rowMeta = new RowMeta();
    Object[] rowData = RowDataUtil.allocateRowData( meta.getFieldName().length + 2 );
    int index = 0;

    if ( meta.isNeverEnding() ) {
      if ( !isEmpty( meta.getRowTimeField() ) ) {
        rowMeta.addValueMeta( new ValueMetaDate( meta.getRowTimeField() ) );
        rowData[index++] = null;
      }

      if ( !isEmpty( meta.getLastTimeField() ) ) {
        rowMeta.addValueMeta( new ValueMetaDate( meta.getLastTimeField() ) );
        rowData[index++] = null;
      }
    }

    for ( int i = 0; i < meta.getFieldName().length; i++ ) {
      int valtype = ValueMetaFactory.getIdForValueMeta( meta.getFieldType()[i] );
      if ( meta.getFieldName()[i] != null ) {
        ValueMetaInterface valueMeta = ValueMetaFactory.createValueMeta( meta.getFieldName()[i], valtype ); // build a
                                                                                                            // value!
        valueMeta.setLength( meta.getFieldLength()[i] );
        valueMeta.setPrecision( meta.getFieldPrecision()[i] );
        valueMeta.setConversionMask( meta.getFieldFormat()[i] );
        valueMeta.setCurrencySymbol( meta.getCurrency()[i] );
        valueMeta.setGroupingSymbol( meta.getGroup()[i] );
        valueMeta.setDecimalSymbol( meta.getDecimal()[i] );
        valueMeta.setOrigin( origin );

        ValueMetaInterface stringMeta =
          ValueMetaFactory.cloneValueMeta( valueMeta, ValueMetaInterface.TYPE_STRING );

        if ( meta.isSetEmptyString() != null && meta.isSetEmptyString()[i] ) {
          // Set empty string
          rowData[index] = StringUtil.EMPTY_STRING;
        } else {
          String stringValue = meta.getValue()[i];
          String stringMinValue = meta.getMinValue()[i];
          String stringMaxValue = meta.getMaxValue()[i];
          String stringEnumValue = meta.getEnumValue()[i];
          String stringPrefixValue = meta.getPrefixValue()[i];
          String stringSuffiexValue = meta.getSuffixValue()[i];
          String stringCharacterTypeValue = meta.getCharacterType()[i];


            // If the value is empty: consider it to be NULL.
          //if ( Utils.isEmpty( stringValue ) ) {
          //  rowData[index] = null;

            if ( valueMeta.getType() == ValueMetaInterface.TYPE_NONE ) {
              String message =
                BaseMessages.getString(
                  PKG, "RowGenerator.CheckResult.SpecifyTypeError", valueMeta.getName(), stringValue );
              remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
            }
          //} else {
            // Convert the data from String to the specified type ...
            //
            try {
              //rowData[index] = valueMeta.convertData( stringMeta, stringValue );
                rowData[index] = buildField(valueMeta, stringValue,stringMinValue,stringMaxValue,stringEnumValue,stringPrefixValue,stringSuffiexValue,stringCharacterTypeValue );
            } catch ( KettleValueException e ) {
              switch ( valueMeta.getType() ) {
                case ValueMetaInterface.TYPE_NUMBER:
                  String message =
                    BaseMessages.getString( PKG, "RowGenerator.BuildRow.Error.Parsing.Number", valueMeta
                      .getName(), stringValue, e.toString() );
                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
                  break;

                case ValueMetaInterface.TYPE_DATE:
                  message =
                    BaseMessages.getString(
                      PKG, "RowGenerator.BuildRow.Error.Parsing.Date", valueMeta.getName(), stringValue, e
                        .toString() );
                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
                  break;

                case ValueMetaInterface.TYPE_INTEGER:
                  message =
                    BaseMessages.getString( PKG, "RowGenerator.BuildRow.Error.Parsing.Integer", valueMeta
                      .getName(), stringValue, e.toString() );
                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
                  break;

                case ValueMetaInterface.TYPE_BIGNUMBER:
                  message =
                    BaseMessages.getString( PKG, "RowGenerator.BuildRow.Error.Parsing.BigNumber", valueMeta
                      .getName(), stringValue, e.toString() );
                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
                  break;

                case ValueMetaInterface.TYPE_TIMESTAMP:
                  message =
                    BaseMessages.getString( PKG, "RowGenerator.BuildRow.Error.Parsing.Timestamp", valueMeta
                      .getName(), stringValue, e.toString() );
                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
                  break;

                default:
                  // Boolean and binary don't throw errors normally, so it's probably an unspecified error problem...
                  message =
                    BaseMessages.getString( PKG, "RowGenerator.CheckResult.SpecifyTypeError", valueMeta
                      .getName(), stringValue );
                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
                  break;

              }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
          }
        //}

        // Now add value to the row!
        // This is in fact a copy from the fields row, but now with data.
        rowMeta.addValueMeta( valueMeta );
        index++;
      }
    }

    return new RowMetaAndData( rowMeta, rowData );
  }

    private static Object buildField(ValueMetaInterface valueMeta, String stringValue, String stringMinValue, String stringMaxValue, String stringEnumValue, String stringPrefixValue, String stringSuffiexValue, String stringCharacterTypeValue) throws KettlePluginException, KettleValueException,Exception {
        ValueMetaInterface stringMeta =
                ValueMetaFactory.cloneValueMeta( valueMeta, ValueMetaInterface.TYPE_STRING );
        java.util.Random random = new java.util.Random();
        Object resultValue= null;
        if(!isEmpty(stringCharacterTypeValue)) {
            if (stringCharacterTypeValue.equals("姓名"))
                resultValue= ZhCharacterUtil.generateName();
            else if (stringCharacterTypeValue.equals("地址"))
                resultValue= ZhCharacterUtil.generateAddr();
            else if (stringCharacterTypeValue.equals("手机号"))
                resultValue= ZhCharacterUtil.generateMobilePhone();
            else if (stringCharacterTypeValue.equals("随机汉字组合")) {
                int leng=valueMeta.getLength();
                if(!isEmpty(stringPrefixValue))
                    leng -= stringPrefixValue.length();
                if(!isEmpty(stringSuffiexValue))
                    leng -= stringSuffiexValue.length();
                //    resultValue=leng+"";
                resultValue= ZhCharacterUtil.generateRandomCharacters(leng);;
            }
            else if (stringCharacterTypeValue.equals("UUID"))
                resultValue= ZhCharacterUtil.generateUUID();
        }
        else  if(!isEmpty(stringEnumValue)) {
             String[] values = stringEnumValue.split(",");
             int index =random.nextInt(values.length);
             resultValue= valueMeta.convertData(stringMeta,values[index]);
         }
         else if (!isEmpty(stringMinValue)&&!isEmpty(stringMaxValue))
         {
             switch (valueMeta.getType())
             {
                 case ValueMetaInterface.TYPE_INTEGER:
                     int intMinValue = new Integer(stringMinValue);
                     int intMaxValue = new Integer(stringMaxValue);
                     int scope = intMaxValue-intMinValue;
                     int randomValue =random.nextInt(scope);
                     resultValue = intMinValue+randomValue;
                     break;
                 case ValueMetaInterface.TYPE_NUMBER:
                 case ValueMetaInterface.TYPE_BIGNUMBER:
                     float numberMinValue = new Integer(stringMinValue);
                     float numberMaxValue = new Integer(stringMaxValue);
                     float numberscope = numberMaxValue-numberMinValue;
                     float randomnumberValue =random.nextFloat()*numberscope;
                     resultValue = numberMinValue+randomnumberValue;
                     break;
                 case ValueMetaInterface.TYPE_DATE:
                     Date dateMinValue = stringMeta.getDate(stringMinValue);
                     Date dateMaxValue= stringMeta.getDate(stringMaxValue);
                     long millSecondDiff = (dateMaxValue.getTime() - dateMinValue.getTime());
                     long randomMillSecondDiff = new Float(random.nextFloat()*millSecondDiff).longValue();
                     resultValue = new Date(dateMinValue.getTime()+randomMillSecondDiff);
                     break;
                 default:
             }
         }
         else if (!isEmpty(stringValue))
         {
             resultValue=stringValue;
         }
      if(valueMeta.getType()==ValueMetaInterface.TYPE_STRING)
      {
          if(!isEmpty(stringPrefixValue))
              resultValue = stringPrefixValue+resultValue;
          if(!isEmpty(stringSuffiexValue))
              resultValue = resultValue +stringSuffiexValue;
      }

        return resultValue;
    }


    @Override
  public synchronized boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (RandomRowGeneratorMeta) smi;
    data = (RandomRowGeneratorData) sdi;

    Object[] r = null;
    boolean retval = true;

    if ( first ) {
      first = false;
        // Create a row (constants) with all the values in it...
        List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>(); // stores the errors...
        RowMetaAndData outputRow = buildRow( meta, remarks, getStepname() );
        if ( !remarks.isEmpty() ) {
            for ( int i = 0; i < remarks.size(); i++ ) {
                CheckResult cr = (CheckResult) remarks.get( i );
                logError( cr.getText() );
            }
            return false;
        }
        data.outputRowMeta = outputRow.getRowMeta();
      getRow();
    } else {
      if ( meta.isNeverEnding() && data.delay > 0 ) {
        try {
          Thread.sleep( data.delay );
        } catch ( InterruptedException e ) {
          throw new KettleException( e );
        }
      }
    }

    if ( ( meta.isNeverEnding() || data.rowsWritten < data.rowLimit ) && !isStopped() ) {
        List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>(); // stores the errors...
        RowMetaAndData outputRow = buildRow( meta, remarks, getStepname() );
        r = outputRow.getData();
    } else {
      setOutputDone(); // signal end to receiver(s)
      return false;
    }

    if ( meta.isNeverEnding() ) {
      data.prevDate = data.rowDate;
      data.rowDate = new Date();

      int index = 0;
      if ( !Utils.isEmpty( meta.getRowTimeField() ) ) {
        r[index++] = data.rowDate;
      }
      if ( !Utils.isEmpty( meta.getLastTimeField() ) ) {
        r[index++] = data.prevDate;
      }
    }

    putRow( data.outputRowMeta, r );
    data.rowsWritten++;

    if ( log.isRowLevel() ) {
      logRowlevel( BaseMessages.getString(
        PKG, "RowGenerator.Log.Wrote.Row", Long.toString( data.rowsWritten ), data.outputRowMeta.getString( r ) ) );
    }

    if ( checkFeedback( data.rowsWritten ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "RowGenerator.Log.LineNr", Long.toString( data.rowsWritten ) ) );
      }
    }

    return retval;
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    try {
      meta = (RandomRowGeneratorMeta) smi;
      data = (RandomRowGeneratorData) sdi;

      if ( super.init( smi, sdi ) ) {
        // Determine the number of rows to generate...
        data.rowLimit = Const.toLong( environmentSubstitute( meta.getRowLimit() ), -1L );
        data.rowsWritten = 0L;
        data.delay = Const.toLong( environmentSubstitute( meta.getIntervalInMs() ), -1L );

        if ( data.rowLimit < 0L ) { // Unable to parse
          logError( BaseMessages.getString( PKG, "RowGenerator.Wrong.RowLimit.Number" ) );
          return false; // fail
        }
        return true;
      }
      return false;
    } catch ( Exception e ) {
      setErrors( 1L );
      logError( "Error initializing step", e );
      return false;
    }
  }

  @Override
  public boolean canProcessOneRow() {
    return true;
  }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }
}
