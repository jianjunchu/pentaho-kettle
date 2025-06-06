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

package org.pentaho.di.trans.steps.tableoutput;

import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.PartitionDatabaseMeta;
import org.pentaho.di.core.exception.*;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.FileUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInput;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Writes rows to a database table.
 *
 * @author Matt Casters
 * @since 6-apr-2003
 */
public class TableOutput extends BaseStep implements StepInterface {
  private static Class<?> PKG = TableOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private TableOutputMeta meta;
  private TableOutputData data;

  public TableOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (TableOutputMeta) smi;
    data = (TableOutputData) sdi;

    Object[] r = getRow(); // this also waits for a previous step to be finished.
    if ( r == null ) { // no more input to be expected...


      // truncate the table if there are no rows at all coming into this step
      if ( first && meta.truncateTable() ) {
        truncateTable();
      }

      /*if(!first){
        emptyAndCommit();
        saveFileInputConf();
      }
*/

      return false;
    }

    if ( first ) {
      first = false;
      if ( meta.truncateTable() ) {
        truncateTable();
      }
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      if ( !meta.specifyFields() ) {
        // Just take the input row
        data.insertRowMeta = getInputRowMeta().clone();
      } else {

        data.insertRowMeta = new RowMeta();

        //
        // Cache the position of the compare fields in Row row
        //
        data.valuenrs = new int[meta.getFieldDatabase().length];
        for ( int i = 0; i < meta.getFieldDatabase().length; i++ ) {
          data.valuenrs[i] = getInputRowMeta().indexOfValue( meta.getFieldStream()[i] );
          if ( data.valuenrs[i] < 0 ) {
            throw new KettleStepException( BaseMessages.getString(
              PKG, "TableOutput.Exception.FieldRequired", meta.getFieldStream()[i] ) );
          }
        }

        for ( int i = 0; i < meta.getFieldDatabase().length; i++ ) {
          ValueMetaInterface insValue = getInputRowMeta().searchValueMeta( meta.getFieldStream()[i] );
          if ( insValue != null ) {
            ValueMetaInterface insertValue = insValue.clone();
            insertValue.setName( meta.getFieldDatabase()[i] );
            data.insertRowMeta.addValueMeta( insertValue );
          } else {
            throw new KettleStepException( BaseMessages.getString(
              PKG, "TableOutput.Exception.FailedToFindField", meta.getFieldStream()[i] ) );
          }
        }
      }
    }

    try {
      Object[] outputRowData = writeToTable( getInputRowMeta(), r );
      if ( outputRowData != null ) {
        putRow( data.outputRowMeta, outputRowData ); // in case we want it go further...
        incrementLinesOutput();

      }

      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isBasic() ) {
          logBasic( "linenr " + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {
      logError( "Because of an error, this step can't continue: ", e );
      setErrors( 1 );
      stopAll();
      setOutputDone(); // signal end to receiver(s)
      return false;
    }

    return true;
  }

  private Object[] lastLine;

  protected Object[] writeToTable( RowMetaInterface rowMeta, Object[] r ) throws KettleException {

    if ( r == null ) { // Stop: last line or error encountered
      if ( log.isDetailed() ) {
        logDetailed( "Last line inserted: stop" );
      }
      return null;
    }
    lastLine  = r;
    saveFileLast(getInputRowMeta(),lastLine);

    PreparedStatement insertStatement = null;
    Object[] insertRowData;
    Object[] outputRowData = r;

    String tableName = null;

    boolean sendToErrorRow = false;
    String errorMessage = null;
    boolean rowIsSafe = false;
    int[] updateCounts = null;
    List<Exception> exceptionsList = null;
    boolean batchProblem = false;
    Object generatedKey = null;

    if ( meta.isTableNameInField() ) {
      // Cache the position of the table name field
      if ( data.indexOfTableNameField < 0 ) {
        String realTablename = environmentSubstitute( meta.getTableNameField() );
        data.indexOfTableNameField = rowMeta.indexOfValue( realTablename );
        if ( data.indexOfTableNameField < 0 ) {
          String message = "Unable to find table name field [" + realTablename + "] in input row";
          logError( message );
          throw new KettleStepException( message );
        }
        if ( !meta.isTableNameInTable() && !meta.specifyFields() ) {
          data.insertRowMeta.removeValueMeta( data.indexOfTableNameField );
        }
      }
      tableName = rowMeta.getString( r, data.indexOfTableNameField );
      if ( !meta.isTableNameInTable() && !meta.specifyFields() ) {
        // If the name of the table should not be inserted itself, remove the table name
        // from the input row data as well. This forcibly creates a copy of r
        //
        insertRowData = RowDataUtil.removeItem( rowMeta.cloneRow( r ), data.indexOfTableNameField );
      } else {
        insertRowData = r;
      }
    } else if ( meta.isPartitioningEnabled()
      && ( meta.isPartitioningDaily() || meta.isPartitioningMonthly() )
      && ( meta.getPartitioningField() != null && meta.getPartitioningField().length() > 0 ) ) {
      // Initialize some stuff!
      if ( data.indexOfPartitioningField < 0 ) {
        data.indexOfPartitioningField =
          rowMeta.indexOfValue( environmentSubstitute( meta.getPartitioningField() ) );
        if ( data.indexOfPartitioningField < 0 ) {
          throw new KettleStepException( "Unable to find field ["
            + meta.getPartitioningField() + "] in the input row!" );
        }

        if ( meta.isPartitioningDaily() ) {
          data.dateFormater = new SimpleDateFormat( "yyyyMMdd" );
        } else {
          data.dateFormater = new SimpleDateFormat( "yyyyMM" );
        }
      }

      ValueMetaInterface partitioningValue = rowMeta.getValueMeta( data.indexOfPartitioningField );
      if ( !partitioningValue.isDate() || r[data.indexOfPartitioningField] == null ) {
        throw new KettleStepException(
          "Sorry, the partitioning field needs to contain a data value and can't be empty!" );
      }

      Object partitioningValueData = rowMeta.getDate( r, data.indexOfPartitioningField );
      tableName =
        environmentSubstitute( meta.getTableName() )
          + "_" + data.dateFormater.format( (Date) partitioningValueData );
      insertRowData = r;
    } else {
      tableName = data.tableName;
      insertRowData = r;
    }

    if ( meta.specifyFields() ) {
      //
      // The values to insert are those in the fields sections
      //
      insertRowData = new Object[data.valuenrs.length];
      for ( int idx = 0; idx < data.valuenrs.length; idx++ ) {
        insertRowData[idx] = r[data.valuenrs[idx]];
      }
    }

    if ( Utils.isEmpty( tableName ) ) {
      throw new KettleStepException( "The tablename is not defined (empty)" );
    }
    if(!data.DDLSynced && meta.createTable() ) // for both create table and alter table. so don't check whether table is exist.
    {
      data.DDLSynced=true;

        String sqlScript = meta.getDDL(tableName,data.db,rowMeta,null,false,null);
//        if(data.databaseMeta.getDatabaseTypeDesc().equals("MYSQL"))
//          if(!data.db.checkTableExists(tableName))
//          {
//            if(meta.getShardKeyField()!=null && meta.getShardKeyField().length()>0)
//              sqlScript = data.db.getDDL(tableName,rowMeta,null, false, meta.getShardKeyField(), true).replace(";", "")+" shardkey="+ meta.getShardKeyField()+" DEFAULT CHARSET=utf8;";
//            else
//              sqlScript = data.db.getDDL(tableName,rowMeta,null, false, null, true).replace(";", "")+"DEFAULT CHARSET=utf8;";
//          }else
//            sqlScript = data.db.getDDL(tableName,rowMeta,null, false, null, true).replace(";", "")+"DEFAULT CHARSET=utf8;";//alter table
//          else
//          sqlScript = data.db.getDDL(tableName,rowMeta,null, false, null, true);

        if(sqlScript!=null && sqlScript.length()>0)
          try{
            if(log.isBasic()) logBasic("create table sqlScript= "+sqlScript);
            execsqlscript(sqlScript);
          }catch (Exception ex)
          {
            ex.printStackTrace();
          }

    }
    insertStatement = data.preparedStatements.get( tableName );
    if ( insertStatement == null ) {
      String sql =
        data.db
          .getInsertStatement( environmentSubstitute( meta.getSchemaName() ), tableName, data.insertRowMeta );
      if ( log.isDetailed() ) {
        logDetailed( "Prepared statement : " + sql );
      }
      insertStatement = data.db.prepareSQL( sql, meta.isReturningGeneratedKeys() );
      data.preparedStatements.put( tableName, insertStatement );
    }

    try {
      // For PG & GP, we add a savepoint before the row.
      // Then revert to the savepoint afterwards... (not a transaction, so hopefully still fast)
      //
      if ( data.useSafePoints ) {
        data.savepoint = data.db.setSavepoint();
      }
      data.db.setValues( data.insertRowMeta, insertRowData, insertStatement );
      data.db.insertRow( insertStatement, data.batchMode, false ); // false: no commit, it is handled in this step differently
      if ( isRowLevel() ) {
        logRowlevel( "Written row: " + data.insertRowMeta.getString( insertRowData ) );
      }

      // Get a commit counter per prepared statement to keep track of separate tables, etc.
      //
      Integer commitCounter = data.commitCounterMap.get( tableName );
      if ( commitCounter == null ) {
        commitCounter = Integer.valueOf( 1 );
      } else {
        commitCounter++;
      }
      data.commitCounterMap.put( tableName, Integer.valueOf( commitCounter.intValue() ) );

      // Release the savepoint if needed
      //
      if ( data.useSafePoints ) {
        if ( data.releaseSavepoint ) {
          data.db.releaseSavepoint( data.savepoint );
        }
      }

      // Perform a commit if needed
      //

      if ( ( data.commitSize > 0 ) && ( ( commitCounter % data.commitSize ) == 0 ) ) {
        if ( data.db.getUseBatchInsert( data.batchMode ) ) {
          try {
            insertStatement.executeBatch();
            data.db.commit();
            insertStatement.clearBatch();

            saveFileInputConf();


          } catch ( SQLException ex ) {
            throw Database.createKettleDatabaseBatchException( "Error updating batch", ex );
          } catch ( Exception ex ) {
            throw new KettleDatabaseException( "Unexpected error inserting row", ex );
          }
        } else {
          // insertRow normal commit
          data.db.commit();

        }


        // Clear the batch/commit counter...
        //
        data.commitCounterMap.put( tableName, Integer.valueOf( 0 ) );
        rowIsSafe = true;

      } else {
        rowIsSafe = false;
      }

      // See if we need to get back the keys as well...
      if ( meta.isReturningGeneratedKeys() ) {
        RowMetaAndData extraKeys = data.db.getGeneratedKeys( insertStatement );

        if ( extraKeys.getRowMeta().size() > 0 ) {
          // Send out the good word!
          // Only 1 key at the moment. (should be enough for now :-)
          generatedKey = extraKeys.getRowMeta().getInteger( extraKeys.getData(), 0 );
        } else {
          // we have to throw something here, else we don't know what the
          // type is of the returned key(s) and we would violate our own rule
          // that a hop should always contain rows of the same type.
          throw new KettleStepException( "No generated keys while \"return generated keys\" is active!" );
        }
      }
    } catch ( KettleDatabaseBatchException be ) {
      errorMessage = be.toString();
      batchProblem = true;
      sendToErrorRow = true;
      updateCounts = be.getUpdateCounts();
      exceptionsList = be.getExceptionsList();

      if ( getStepMeta().isDoingErrorHandling() ) {
        data.db.clearBatch( insertStatement );
        data.db.commit( true );
      } else {
        data.db.clearBatch( insertStatement );
        data.db.rollback();
        StringBuilder msg = new StringBuilder( "Error batch inserting rows into table [" + tableName + "]." );
        msg.append( Const.CR );
        msg.append( "Errors encountered (first 10):" ).append( Const.CR );
        for ( int x = 0; x < be.getExceptionsList().size() && x < 10; x++ ) {
          Exception exception = be.getExceptionsList().get( x );
          if ( exception.getMessage() != null ) {
            msg.append( exception.getMessage() ).append( Const.CR );
          }
        }
        throw new KettleException( msg.toString(), be );
      }
    } catch ( KettleDatabaseException dbe ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        if ( isRowLevel() ) {
          logRowlevel( "Written row to error handling : " + getInputRowMeta().getString( r ) );
        }

        if ( data.useSafePoints ) {
          data.db.rollback( data.savepoint );
          if ( data.releaseSavepoint ) {
            data.db.releaseSavepoint( data.savepoint );
          }
          // data.db.commit(true); // force a commit on the connection too.
        }

        sendToErrorRow = true;
        errorMessage = dbe.toString();
      } else {
        if ( meta.ignoreErrors() ) {
          if ( data.warnings < 20 ) {
            if ( log.isBasic() ) {
              logBasic( "WARNING: Couldn't insert row into table: "
                + rowMeta.getString( r ) + Const.CR + dbe.getMessage() );
            }
          } else if ( data.warnings == 20 ) {
            if ( log.isBasic() ) {
              logBasic( "FINAL WARNING (no more then 20 displayed): Couldn't insert row into table: "
                + rowMeta.getString( r ) + Const.CR + dbe.getMessage() );
            }
          }
          data.warnings++;
        } else {
          setErrors( getErrors() + 1 );
          data.db.rollback();
          throw new KettleException( "Error inserting row into table ["
            + tableName + "] with values: " + rowMeta.getString( r ), dbe );
        }
      }
    }catch ( Exception e ){
      if ( getStepMeta().isDoingErrorHandling()){
        if ( isRowLevel()){
          logRowlevel( "Written row to error handling : " + getInputRowMeta().getString( r ) );
        }

        if ( data.useSafePoints ) {
          data.db.rollback( data.savepoint );
          if ( data.releaseSavepoint ) {
            data.db.releaseSavepoint( data.savepoint );
          }
          // data.db.commit(true); // force a commit on the connection too.
        }

        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        if ( meta.ignoreErrors() ) {
          if ( data.warnings < 20 ) {
            if ( log.isBasic() ) {
              logBasic( "WARNING: Couldn't insert row into table: "
                      + rowMeta.getString( r ) + Const.CR + e.getMessage() );
            }
          } else if ( data.warnings == 20 ) {
            if ( log.isBasic() ) {
              logBasic( "FINAL WARNING (no more then 20 displayed): Couldn't insert row into table: "
                      + rowMeta.getString( r ) + Const.CR + e.getMessage() );
            }
          }
          data.warnings++;
        } else {
          setErrors( getErrors() + 1 );
          data.db.rollback();
          throw new KettleException( "Error inserting row into table ["
                  + tableName + "] with values: " + rowMeta.getString( r ), e );
        }
      }
    }

    // We need to add a key
    if ( generatedKey != null ) {
      outputRowData = RowDataUtil.addValueData( outputRowData, rowMeta.size(), generatedKey );
    }

    if ( data.batchMode ) {
      if ( sendToErrorRow ) {
        if ( batchProblem ) {
          data.batchBuffer.add( outputRowData );
          outputRowData = null;

          processBatchException( errorMessage, updateCounts, exceptionsList );
        } else {
          // Simply add this row to the error row
          putError( rowMeta, r, 1L, errorMessage, null, "TOP001" );
          outputRowData = null;
        }
      } else {
        data.batchBuffer.add( outputRowData );
        outputRowData = null;

        if ( rowIsSafe ) { // A commit was done and the rows are all safe (no error)



          for ( int i = 0; i < data.batchBuffer.size(); i++ ) {
            Object[] row = data.batchBuffer.get( i );
            putRow( data.outputRowMeta, row );
            incrementLinesOutput();
          }
          // Clear the buffer
          data.batchBuffer.clear();
        }
      }
    } else {
      if ( sendToErrorRow ) {
        putError( rowMeta, r, 1, errorMessage, null, "TOP001" );
        outputRowData = null;
      }
    }



    return outputRowData;
  }

  Map<String, String> filelastLineNumber = new HashMap<>();

  /**
   * 保存每个文件最后一行到内存
   * @param rowMeta
   * @param r
   */
  private void saveFileLast(RowMetaInterface rowMeta, Object[] r){
//记录文本文件输入读的文件名和已读行数
    int file_input_read_line_index = rowMeta.indexOfValue("FILE_INPUT_READ_LINE");
    int file_input_file_name_index = rowMeta.indexOfValue("FILE_INPUT_FILE_NAME");
    int file_input_file_path_index = rowMeta.indexOfValue("FILE_INPUT_FILE_PATH");
    if(file_input_read_line_index > 0 && file_input_file_name_index > 0 && file_input_file_path_index > 0){

      Object file_input_read_line = r[file_input_read_line_index];
      String file_input_file_name = String.valueOf(r[file_input_file_name_index]) ;
      Object file_input_file_path = r[file_input_file_path_index];

      JSONObject jsonObject = new JSONObject();
      jsonObject.put("file_input_read_line",file_input_read_line);
      jsonObject.put("file_input_file_name",file_input_file_name);
      jsonObject.put("file_input_file_path",file_input_file_path);

      File data = FileUtils.getFile(String.valueOf(file_input_file_path),file_input_file_name);
      filelastLineNumber.put(data.getAbsolutePath(),jsonObject.toJSONString());

    }
  }

  private void saveFileInputConf()  {

    try {
      Set<String> set = filelastLineNumber.keySet();
      for (String key : set) {
        String json = filelastLineNumber.get(key);
        File data = FileUtils.getFile(key+".conf");
        FileUtils.write(data,json);

      }
    }catch (Exception e){
      logError( "记录文件行数失败: "
               + Const.CR + e.getMessage() );
    }

  }

  public boolean isRowLevel() {
    return log.isRowLevel();
  }

  private void processBatchException( String errorMessage, int[] updateCounts, List<Exception> exceptionsList ) throws KettleException {
    // There was an error with the commit
    // We should put all the failing rows out there...
    //
    if ( updateCounts != null ) {
      int errNr = 0;
      for ( int i = 0; i < updateCounts.length; i++ ) {
        Object[] row = data.batchBuffer.get( i );
        if ( updateCounts[i] > 0 ) {
          // send the error foward
          putRow( data.outputRowMeta, row );
          incrementLinesOutput();
        } else {
          String exMessage = errorMessage;
          if ( errNr < exceptionsList.size() ) {
            SQLException se = (SQLException) exceptionsList.get( errNr );
            errNr++;
            exMessage = se.toString();
          }
          putError( data.outputRowMeta, row, 1L, exMessage, null, "TOP0002" );
        }
      }
    } else {
      // If we don't have update counts, it probably means the DB doesn't support it.
      // In this case we don't have a choice but to consider all inserted rows to be error rows.
      //
      for ( int i = 0; i < data.batchBuffer.size(); i++ ) {
        Object[] row = data.batchBuffer.get( i );
        putError( data.outputRowMeta, row, 1L, errorMessage, null, "TOP0003" );
      }
    }

    // Clear the buffer afterwards...
    data.batchBuffer.clear();
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (TableOutputMeta) smi;
    data = (TableOutputData) sdi;

    if ( super.init( smi, sdi ) ) {
      try {
        data.commitSize = Integer.parseInt( environmentSubstitute( meta.getCommitSize() ) );

        data.databaseMeta = meta.getDatabaseMeta();
        DatabaseInterface dbInterface = data.databaseMeta.getDatabaseInterface();

        // Batch updates are not supported on PostgreSQL (and look-a-likes)
        // together with error handling (PDI-366).
        // For these situations we can use savepoints to help out.
        //
        data.useSafePoints =
          data.databaseMeta.getDatabaseInterface().useSafePoints() && getStepMeta().isDoingErrorHandling();

        // Get the boolean that indicates whether or not we can/should release
        // savepoints during data load.
        //
        data.releaseSavepoint = dbInterface.releaseSavepoint();

        // Disable batch mode in case
        // - we use an unlimited commit size
        // - if we need to pick up auto-generated keys
        // - if you are running the transformation as a single database transaction (unique connections)
        // - if we are reverting to save-points
        //
        data.batchMode =
          meta.useBatchUpdate()
            && data.commitSize > 0 && !meta.isReturningGeneratedKeys()
            && !getTransMeta().isUsingUniqueConnections() && !data.useSafePoints;

        // Per PDI-6211 : give a warning that batch mode operation in combination with step error handling can lead to
        // incorrectly processed rows.
        //
        if ( getStepMeta().isDoingErrorHandling() && !dbInterface.supportsErrorHandlingOnBatchUpdates() ) {
          log.logMinimal( BaseMessages.getString(
            PKG, "TableOutput.Warning.ErrorHandlingIsNotFullySupportedWithBatchProcessing" ) );
        }

        if ( meta.getDatabaseMeta() == null ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "TableOutput.Exception.DatabaseNeedsToBeSelected" ) );
        }
        if ( meta.getDatabaseMeta() == null ) {
          logError( BaseMessages.getString( PKG, "TableOutput.Init.ConnectionMissing", getStepname() ) );
          return false;
        }

        if ( !dbInterface.supportsStandardTableOutput() ) {
          throw new KettleException( dbInterface.getUnsupportedTableOutputMessage() );
        }

        data.db = new Database( this, meta.getDatabaseMeta() );
        data.db.shareVariablesWith( this );

        if ( getTransMeta().isUsingUniqueConnections() ) {
          synchronized ( getTrans() ) {
            data.db.connect( getTrans().getTransactionId(), getPartitionID() );
          }
        } else {
          data.db.connect( getPartitionID() );
        }

        if ( log.isBasic() ) {
          logBasic( "Connected to database [" + meta.getDatabaseMeta() + "] (commit=" + data.commitSize + ")" );
        }

        // Postpone commit as long as possible. PDI-2091
        //
        if ( data.commitSize == 0 ) {
          data.commitSize = Integer.MAX_VALUE;
        }
        data.db.setCommit( data.commitSize );

        if ( !meta.isPartitioningEnabled() && !meta.isTableNameInField() ) {
          data.tableName = environmentSubstitute( meta.getTableName() );
        }

        if(data.tablesqlfirst&&meta.createTable())
            //        if(data.tablesqlfirst) // for both create table and alter table. so don't check whether table is exist.
        {
          data.tablesqlfirst=false;
          String schemaTable= meta.getDatabaseMeta().getQuotedSchemaTableCombination(environmentSubstitute(meta.getSchemaName()), environmentSubstitute(data.tableName));
          RowMetaInterface rmi = this.getTransMeta().getStepFields(this.getStepname());
          if(!data.db.checkTableExists(schemaTable))
          {
            String sqlScript="";
            if(data.databaseMeta.getDatabaseTypeDesc().equals("MYSQL")) {
              //SearchFieldsProgressDialog op = new SearchFieldsProgressDialog(this.getTransMeta(), this.getTransMeta().getStep(), true);
              sqlScript = data.db.getDDL(schemaTable, rmi, null, false, null, true).replace(";", "") + "DEFAULT CHARSET=utf8;";
            }
            else
              sqlScript = data.db.getDDL(schemaTable,rmi,null, false, null, true);

            if(sqlScript!=null && sqlScript.length()>0)
              try{
                if(log.isBasic()) logBasic("CREATE TABLE DDL ["+sqlScript+"]");
                execsqlscript(sqlScript);
              }catch (Exception ex)
              {
                ex.printStackTrace();
              }
          }else
          {
            String sqlScript="";
            if(data.databaseMeta.getDatabaseTypeDesc().equals("MYSQL")) {

              sqlScript = data.db.getDDL(schemaTable, rmi, null, false, null, true);
            }
            else
              sqlScript = data.db.getDDL(schemaTable,rmi,null, false, null, true);

            if(sqlScript!=null && sqlScript.length()>0)
              try{
                if(log.isBasic()) logBasic("ALTER TABLE DDL ["+sqlScript+"]");
                execsqlscript(sqlScript);
              }catch (Exception ex)
              {
                ex.printStackTrace();
              }
          }
        }

        return true;
      } catch ( KettleException e ) {
        logError( "An error occurred intialising this step: " + e.getMessage() );
        stopAll();
        setErrors( 1 );
      }
    }
    return false;
  }

  void truncateTable() throws KettleDatabaseException {
    if ( !meta.isPartitioningEnabled() && !meta.isTableNameInField() ) {
      // Only the first one truncates in a non-partitioned step copy
      //
      if ( meta.truncateTable()
        && ( ( getCopy() == 0 && getUniqueStepNrAcrossSlaves() == 0 ) || !Utils.isEmpty( getPartitionID() ) ) ) {
        data.db.truncateTable( environmentSubstitute( meta.getSchemaName() ), environmentSubstitute( meta
          .getTableName() ) );

      }
    }
  }


  private void  emptyAndCommit(){
    if ( data.db != null ) {
      try {
        for ( String schemaTable : data.preparedStatements.keySet() ) {
          // Get a commit counter per prepared statement to keep track of separate tables, etc.
          //
          Integer batchCounter = data.commitCounterMap.get( schemaTable );
          if ( batchCounter == null ) {
            batchCounter = 0;
          }

          PreparedStatement insertStatement = data.preparedStatements.get( schemaTable );

          data.db.emptyAndCommit( insertStatement, data.batchMode, batchCounter );

          saveFileInputConf();
        }



        for ( int i = 0; i < data.batchBuffer.size(); i++ ) {
          Object[] row = data.batchBuffer.get( i );
          putRow( data.outputRowMeta, row );
          incrementLinesOutput();

        }
        // Clear the buffer
        data.batchBuffer.clear();
      } catch ( KettleDatabaseBatchException be ) {
        if ( getStepMeta().isDoingErrorHandling() ) {
          // Right at the back we are experiencing a batch commit problem...
          // OK, we have the numbers...
          try {
            processBatchException( be.toString(), be.getUpdateCounts(), be.getExceptionsList() );
          } catch ( KettleException e ) {
            logError( "Unexpected error processing batch error", e );
            setErrors( 1 );
            stopAll();
          }
        } else {
          logError( "Unexpected batch update error committing the database connection.", be );
          setErrors( 1 );
          stopAll();
        }
      } catch ( Exception dbe ) {
        logError( "Unexpected error committing the database connection.", dbe );
        logError( Const.getStackTracker( dbe ) );
        setErrors( 1 );
        stopAll();
      }

    }
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (TableOutputMeta) smi;
    data = (TableOutputData) sdi;

    if ( data.db != null ) {
      try {
        for ( String schemaTable : data.preparedStatements.keySet() ) {
          // Get a commit counter per prepared statement to keep track of separate tables, etc.
          //
          Integer batchCounter = data.commitCounterMap.get( schemaTable );
          if ( batchCounter == null ) {
            batchCounter = 0;
          }
          PreparedStatement insertStatement = data.preparedStatements.get( schemaTable );
          data.db.emptyAndCommit( insertStatement, data.batchMode, batchCounter );
          saveFileInputConf();
        }



        for ( int i = 0; i < data.batchBuffer.size(); i++ ) {
          Object[] row = data.batchBuffer.get( i );
          putRow( data.outputRowMeta, row );
          incrementLinesOutput();

        }
        // Clear the buffer
        data.batchBuffer.clear();
        StepMetaInterface sii = getTransMeta().getStep( 0 ).getStepMetaInterface();
       if(sii instanceof TextFileInputMeta){
         TextFileInputMeta textFileInputMeta = (TextFileInputMeta) sii;
        String backupPath = environmentSubstitute(textFileInputMeta.getBackupPath());

         if(textFileInputMeta.isBackupFile() && getTrans().getErrors() ==0 && !StringUtils.isEmpty(backupPath)){
           logBasic("开始备份文件....");
           FileUtils.forceMkdir(new File(backupPath));

           for(String path : textFileInputMeta.getProcessedFile()){
             logBasic("备份目录:"+ backupPath);
             File srcFile = new File(path);
             File destFile = new File(backupPath,srcFile.getName());

             if(srcFile.exists()){
               FileUtils.moveFile(srcFile,destFile);
             }

             srcFile = new File(path+".conf");
             destFile = new File(backupPath ,srcFile.getName());
             if(srcFile.exists()){
               if(destFile.exists()){
                 FileUtils.deleteQuietly(destFile);
               }
               FileUtils.moveFile(srcFile,destFile);
             }

           }
         }
       }

      } catch ( KettleDatabaseBatchException be ) {
        if ( getStepMeta().isDoingErrorHandling() ) {
          // Right at the back we are experiencing a batch commit problem...
          // OK, we have the numbers...
          try {
            processBatchException( be.toString(), be.getUpdateCounts(), be.getExceptionsList() );
          } catch ( KettleException e ) {
            logError( "Unexpected error processing batch error", e );
            setErrors( 1 );
            stopAll();
          }
        } else {
          logError( "Unexpected batch update error committing the database connection.", be );
          setErrors( 1 );
          stopAll();
        }
      } catch ( Exception dbe ) {
        logError( "Unexpected error committing the database connection.", dbe );
        logError( Const.getStackTracker( dbe ) );
        setErrors( 1 );
        stopAll();
      } finally {
        setOutputDone();

        if ( getErrors() > 0 ) {
          try {
            data.db.rollback();
          } catch ( KettleDatabaseException e ) {
            logError( "Unexpected error rolling back the database connection.", e );
          }
        }

        data.db.disconnect();
      }
      super.dispose( smi, sdi );
    }
  }

  /**
   * Allows subclasses of TableOuput to get hold of the step meta
   *
   * @return
   */
  protected TableOutputMeta getMeta() {
    return meta;
  }

  /**
   * Allows subclasses of TableOutput to get hold of the data object
   *
   * @return
   */
  protected TableOutputData getData() {
    return data;
  }

  protected void setMeta( TableOutputMeta meta ) {
    this.meta = meta;
  }

  protected void setData( TableOutputData data ) {
    this.data = data;
  }

  private void execsqlscript(String sqlScript)
  {
    DatabaseMeta ci = data.databaseMeta;
    if (ci==null) return;

    StringBuffer message = new StringBuffer();

    Database db = new Database(ci);
    boolean first = true;
    PartitionDatabaseMeta[] partitioningInformation = ci.getPartitioningInformation();

    for (int partitionNr=0;first || (partitioningInformation!=null && partitionNr<partitioningInformation.length) ; partitionNr++)
    {
      first = false;
      String partitionId = null;
      if (partitioningInformation!=null && partitioningInformation.length>0)
      {
        partitionId = partitioningInformation[partitionNr].getPartitionId();
      }
      try
      {
        db.connect(partitionId);

        // Multiple statements in the script need to be split into individual executable statements
        List<String> statements = ci.getDatabaseInterface().parseStatements(sqlScript + Const.CR);

        int nrstats = 0;
        for(String sql : statements) {
          log.logDetailed("launch DDL statement: "+Const.CR+sql);

          // A DDL statement
          nrstats++;
          try
          {
            log.logDetailed("Executing SQL: "+Const.CR+sql);
            db.execStatement(sql);
            message.append(BaseMessages.getString(PKG, "SQLEditor.Log.SQLExecuted", sql));
            message.append(Const.CR);
          }
          catch(Exception dbe)
          {
            String error = BaseMessages.getString(PKG, "SQLEditor.Log.SQLExecError", sql, dbe.toString());
            message.append(error).append(Const.CR);
          }
        }
        message.append(BaseMessages.getString(PKG, "SQLEditor.Log.StatsExecuted", Integer.toString(nrstats)));
        if (partitionId!=null)
          message.append(BaseMessages.getString(PKG, "SQLEditor.Log.OnPartition", partitionId));
        message.append(Const.CR);
      }
      catch(KettleDatabaseException dbe)
      {
        String error = BaseMessages.getString(PKG, "SQLEditor.Error.CouldNotConnect.Message", (data.databaseMeta==null ? "" : data.databaseMeta.getName()), dbe.getMessage());
        message.append(error).append(Const.CR);
      }
      finally
      {
        db.disconnect();
      }
    }
  }
}
