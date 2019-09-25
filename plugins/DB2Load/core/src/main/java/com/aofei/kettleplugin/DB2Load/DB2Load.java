/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package com.aofei.kettleplugin.DB2Load;

import java.io.*;
import java.util.*;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.*;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * DB2 batch load
 *
 * @author Jason
 */

public class DB2Load extends BaseStep implements StepInterface {
    private static Class<?> PKG = DB2LoadMeta.class; // for i18n purposes,

    private DB2LoadMeta meta;
    private DB2LoadData data;

    private boolean db2LoadIsEnd;

    private static boolean loadError = false;

    private Runtime rt = Runtime.getRuntime();

    public DB2Load(StepMeta stepMeta, StepDataInterface stepDataInterface,
                   int copyNr, TransMeta transMeta, Trans trans) {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    public  boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
            throws KettleException {

        meta = (DB2LoadMeta) smi;
        data = (DB2LoadData) sdi;
        boolean result = true;

        if(loadError){
            setErrors(1);
            stopAll();
            return  false;
        }

        Object[] r = getRow();
        if (r != null && first) {

            first = false;
            data.outputRowMeta = getInputRowMeta().clone();

            if(meta.getFieldTable()!=null && meta.getFieldTable().length>0){
                Object[] array = meta.getFieldTable();
                String separator = ",";
                data.fieldTables = joinArray(array, separator);
            }else{
                Object[] array = data.outputRowMeta.getFieldNames();
                String separator = ",";
                data.fieldTables = joinArray(array, separator);
            }
            log.logBasic("Table fields:"+data.fieldTables);

            if(meta.getFieldStream()!=null  && meta.getFieldStream().length > 0){
                data.keynrs = new int[meta.getFieldStream().length];
                for (int i=0;i<data.keynrs.length;i++) {
                    data.keynrs[i] = getInputRowMeta().indexOfValue(meta.getFieldStream()[i]);
                }
            }else{
                data.keynrs = new int[data.outputRowMeta.getFieldNames().length];
                for (int i=0;i<data.outputRowMeta.getFieldNames().length;i++) {
                    data.keynrs[i] = data.outputRowMeta.indexOfValue(data.outputRowMeta.getFieldNames()[i]);
                }
            }

            try {
                openFifoAndLoadData();
            } catch (Exception e) {
                logError("Error in step, asking everyone to stop because of:", e); //$NON-NLS-1$
                setErrors(1);
                stopAll();
                setOutputDone();  // signal end to receiver(s)
                return false;
            }
        }

        if (r == null) // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
        /**
         */
        if(data.tarTableDefine == null){
            String tabName = "";
            String schema = "";
            if(meta.getSchemaName()==null || meta.getSchemaName().length()==0){
                tabName = environmentSubstitute(meta.getTableName());
        		String[] arry = tabName.split("\\.");
        		switch (arry.length) {
				case 1:
					schema = environmentSubstitute(meta.getDatabaseMeta().getUsername());
                    tabName = arry[0];
					break;
				case 2:
					schema  = arry[0];
	                tabName = arry[1];
					break;
				default:
					schema  = arry[0];
	                tabName = arry[1];
					break;
				}
            }else{
                schema = environmentSubstitute(meta.getSchemaName());
                tabName= environmentSubstitute(meta.getTableName());
            }
            if(data.db.getConnection() == null){
                data.db.connect();
            }

            data.tarTableDefine = data.db.getTarTableDefines(tabName,schema);
            data.db.disconnect();
        }

        /**
         * Insert Or Load in table we need to substring
         */
        subColBeforInTable(data.tarTableDefine, r, data.outputRowMeta);
        //
        writeRowToFile(data.outputRowMeta, r);

        putRow(data.outputRowMeta, r); // in case we want it to go further...

        if (checkFeedback(getLinesOutput()))
            logBasic("linenr " + getLinesOutput());

        return result;

    }
    private void openFifoAndLoadData() throws Exception{

        logBasic("Opening fifo " + data.fifoFilename + " for writing.");
        OpenFifo openFifo = new OpenFifo(data, 1024,getTransMeta());
        openFifo.start();
        data.sqlRunner = new SqlRunner(getCopy());
        data.sqlRunner.start();

        while (true){
            openFifo.join(200);
            if (openFifo.getState() == Thread.State.TERMINATED){
                break;
            }
            try{
                data.sqlRunner.checkExcn();
            }
            catch (Exception e){
                // We need to open a stream to the fifo to unblock the fifo writer
                // that was waiting for the sqlRunner that now isn't running
                new BufferedInputStream( new FileInputStream( data.fifoFilename )).close();
                openFifo.join();
                setErrors(1);
                logError("Make sure user has been granted the FILE privilege.");
                throw e;
            }

            try{
                openFifo.checkExcn();
            }
            catch (Exception e){
                setErrors(1);
                throw e;
            }

        }
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (DB2LoadMeta) smi;
        data = (DB2LoadData) sdi;

        try {
            getTrans().finishedThreads.getAndIncrement();

            if(getCopy() ==0 ){
                Thread.sleep(2000);
                removeFile();
            }

            if (data.fos != null) {
                data.fos.close();
            }
            if(data.db.getConnection() != null){
                data.db.disconnect();
            }
        } catch (Exception e) {
            logError("Unexpected error closing file", e);
            setErrors(1);
        }
        super.dispose(smi, sdi);
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (DB2LoadMeta) smi;
        data = (DB2LoadData) sdi;
        db2LoadIsEnd  = false;
        if (super.init(smi, sdi)) {

            try {
                if(meta.getDatabaseMeta() == null){
                    throw new KettleException("Connection is not exists");
                }
                data.db = new Database(this, meta.getDatabaseMeta());
                data.db.shareVariablesWith(this);
            } catch (Exception e) {
                logError("Init Connection Error :",e.getMessage());
                setErrors(1L);
                stopAll();
                return false;
            }

            try {
                //Tony 20171218 init temp data file name
                data.fifoFilename = initFifoFilename(meta.getDataFile());
            } catch (Exception e) {
                logError("Init fifo Filename Error :",e.getMessage());
                setErrors(1L);
                stopAll();
                return false;
            }



            // Add init code here.
            if (getCopy() == 0) {
                try {
                    if (connectToDB2()) {
                        String sql = environmentSubstitute(meta.getBeforSQL());
                        insertSql(sql);
                    }

                } catch (Exception e) {
                    getTrans().setVariable(Const.KETTLE_TRANS_PAN_JVM_EXIT_CODE, "11");
                    logError(Messages.getString("DB2Load.ExecError.BeforSQL",e.getMessage()));
                    setErrors(1L);
                    stopAll();
                    return false;
                }
                try {
                    executeTruncate();
                } catch (Exception e) {
                    logError("Execute truncate error " + e.getMessage());
                    setErrors(1L);
                    stopAll();
                    return false;
                }
                try {
                    createNamedPipe();
                } catch (Exception e) {
                    logError(Messages.getString("DB2Load.mkfifoCommandErro",e.getMessage()));
                    setErrors(1L);
                    stopAll();
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private String joinArray(Object[] array,String separator){
        StringBuffer stringBuffer = new StringBuffer();
        for(int i=0 ; i < array.length ; i++){
            stringBuffer.append(array[i]);
            if(i < array.length-1){
                stringBuffer.append(separator);
            }
        }
        return stringBuffer.toString();
    }
    private String initFifoFilename(String dataFile) throws KettleException {
        String fifoPath =  environmentSubstitute(dataFile);
        if(StringUtil.isEmpty(fifoPath)){
            throw new KettleException(Messages.getString("DB2Load.Exception.noDataPath"));
        }
        String filename = getTrans().getLogChannelId().replaceAll("-","");
        return fifoPath.endsWith(File.separator) ? fifoPath + filename : fifoPath + File.separator + filename;
    }

    private void createNamedPipe() throws Exception {

        Runtime rt = Runtime.getRuntime();

        File fifoFile = new File(data.fifoFilename);

        createParentFolder(data.fifoFilename);

        if (!fifoFile.exists()) {

            // MKFIFO!
            //
            String mkFifoCmd = "mkfifo " + data.fifoFilename;
            logBasic("Creating FIFO file using this command : " + mkFifoCmd);
            Process mkFifoProcess = rt.exec(mkFifoCmd);
            StreamLogger errorLogger = new StreamLogger(log, mkFifoProcess.getErrorStream(), "mkFifoError");
            StreamLogger outputLogger = new StreamLogger(log, mkFifoProcess.getInputStream(), "mkFifoOuptut");
            new Thread(errorLogger).start();
            new Thread(outputLogger).start();
            int result = mkFifoProcess.waitFor();
            if (result != 0) {
                throw new KettleException("Return code " + result + " received from statement : " + mkFifoCmd);
            }

            String chmodCmd = "chmod 666 " + data.fifoFilename;
            logBasic("Setting FIFO file permissings using this command : " + chmodCmd);
            Process chmodProcess = rt.exec(chmodCmd);
            errorLogger = new StreamLogger(log, chmodProcess.getErrorStream(), "chmodError");
            outputLogger = new StreamLogger(log, chmodProcess.getInputStream(), "chmodOuptut");
            new Thread(errorLogger).start();
            new Thread(outputLogger).start();
            result = chmodProcess.waitFor();
            if (result != 0) {
                throw new KettleException("Return code " + result + " received from statement : " + chmodCmd);
            }
        }


    }


    public boolean checkPreviouslyOpened(String filename) {

        return data.previouslyOpenedFiles.contains(filename);

    }

    private void writeRowToFile(RowMetaInterface rowMeta, Object[] r) throws KettleStepException {
        try {
            /*
             * Write all values in stream to text file.
			 */
            for (int i=0;i<data.keynrs.length;i++) {
                if (i > 0 ) {
                    data.fifoWriter.write(data.binarySeparator);
                }
                int index = data.keynrs[i];
                ValueMetaInterface v = rowMeta.getValueMeta(index);
                Object valueData = r[index];

                writeField(v, valueData);
            }

            data.fifoWriter.write(data.binaryNewline);
            //解决串行问题
            data.fifoWriter.flush();

            incrementLinesOutput();

            // flush every 4k lines
            // if (linesOutput>0 && (linesOutput&0xFFF)==0) data.writer.flush();
        } catch (Exception e) {

            throw new KettleStepException("Error writing line", e);

        }
    }
    private byte[] formatField(ValueMetaInterface v, Object valueData) throws KettleValueException {
        if(valueData == null){
            return null;
        }
        return v.getBinaryString4DB(valueData);
    }
    private void writeField(ValueMetaInterface v, Object valueData) throws KettleStepException {
        try {

            byte[] bytes = formatField(v, valueData);

            if (bytes != null) {

                List<Integer> enclosures = null;

                if (v.getType() == ValueMetaInterface.TYPE_STRING || v.getType() == ValueMetaInterface.TYPE_BINARY) {
                    data.fifoWriter.write(data.binaryEnclosure);
                    enclosures = getEnclosurePositions(bytes);

                    //data.fifoWriter.write(bytes);

                    if (enclosures == null) {
                        data.fifoWriter.write(bytes);
                    } else {
                        // Skip the enclosures, double them instead...
                        int from = 0;
                        for (int i = 0; i < enclosures.size(); i++) {
                            int position = enclosures.get(i);
                            data.fifoWriter.write(bytes, from, position + data.binaryEnclosure.length - from);
                            data.fifoWriter.write(data.binaryEnclosure); // write enclosure a second time
                            from = position + data.binaryEnclosure.length;
                        }
                        if (from < bytes.length) {
                            data.fifoWriter.write(bytes, from, bytes.length - from);
                        }
                    }

                    data.fifoWriter.write(data.binaryEnclosure);
                }else{
                	data.fifoWriter.write(bytes);
                }
                
            }
        } catch (Exception e) {
            throw new KettleStepException("Error writing field content to file", e);
        }
    }


    private List<Integer> getEnclosurePositions(byte[] str) {
        List<Integer> positions = null;
        if (data.binaryEnclosure != null && data.binaryEnclosure.length > 0) {
            for (int i = 0; i < str.length - data.binaryEnclosure.length + 1; i++) {
                // verify if on position i there is an enclosure
                //
                boolean found = true;
                for (int x = 0; found && x < data.binaryEnclosure.length; x++) {
                    if (str[i + x] != data.binaryEnclosure[x])
                        found = false;
                }
                if (found) {
                    if (positions == null)
                        positions = new ArrayList<Integer>();
                    positions.add(i);
                }
            }
        }
        return positions;
    }

    private boolean connectToDB2() throws Exception {
        StringBuffer sb = new StringBuffer();

        String pass = environmentSubstitute(meta.getDatabaseMeta().getPassword());
        pass =  Encr.decryptPasswordOptionallyEncrypted(pass);
        String user = environmentSubstitute(meta.getDatabaseMeta().getUsername());
        //alias
        String databaseName = environmentSubstitute(meta.getDatabaseName());
        sb.append("db2 connect to  ").append(databaseName).append(" user ").append(user).append(" using ").append("'"+pass+"'");

        String db2ConnectCmd = sb.toString();
        logBasic("satrt exec : " + db2ConnectCmd.substring(0,db2ConnectCmd.indexOf("using")));
        Process db2ConnectProcess = rt.exec(db2ConnectCmd);
        StreamLogger errorLogger = new StreamLogger(log, db2ConnectProcess.getErrorStream(), "db2ConnectError");
        StreamLogger outputLogger = new StreamLogger(log, db2ConnectProcess.getInputStream(), "db2ConnectOuptut");
        new Thread(errorLogger).start();
        new Thread(outputLogger).start();
        int result = db2ConnectProcess.waitFor();
        if (result != 0) {
            throw new KettleDatabaseException("Return code " + result + " received from statement : db2 connect to");
        }

        return true;
    }


    // Class to try and open a writer to a fifo in a different thread.
    // Opening the fifo is a blocking call, so we need to check for errors
    // after a small waiting period
    static class OpenFifo extends Thread
    {
        private  DB2LoadData data;
        private Exception ex;
        private int size;
        TransMeta transMeta;

        OpenFifo(DB2LoadData data, int size, TransMeta transMeta)
        {
            this.data = data;
            this.size = size;
            this.transMeta = transMeta;
        }

        public void run()
        {
            try{
                data.fos = KettleVFS.getOutputStream(data.fifoFilename, transMeta, true);
                data.fifoWriter  = new BufferedOutputStream(data.fos,5120000);

            } catch (Exception ex) {
                this.ex = ex;
            }
        }

        void checkExcn() throws Exception
        {
            // This is called from the main thread context to rethrow any saved
            // excn.
            if (ex != null) {
                throw ex;
            }
        }
    }

    class SqlRunner extends Thread
    {

        int stepcopy;

        public SqlRunner(int stepcopy){
            this.stepcopy = stepcopy;
        }

        private Exception ex;

        public void run()
        {
            try {
                if(stepcopy ==0){
                    executeLoadShell();
                }
            } catch (Exception ex) {
                this.ex = ex;
            }
        }

        void checkExcn() throws Exception
        {
            // This is called from the main thread context to rethrow any saved
            // excn.
            if (ex != null) {
                throw ex;
            }
        }
    }


    private void executeTruncate() throws Exception {

        if (meta.getLoadAction().endsWith(DB2LoadMeta.ACTION_TRUNCATE)) {
            log.logBasic("start truncate table ");
            //tony 2018
            String tableName;
            if(meta.getSchemaName()==null || meta.getSchemaName().length()==0)
                tableName= meta.getTableName();
            else
                tableName= meta.getSchemaName()+"."+meta.getTableName();

            String truncateAction = meta.getTruncateAction();
            if ( DB2LoadMeta.TRUNCATE_ACTION_DEFAULT.equals(truncateAction))
            {
                log.logBasic("run truncate table default");
                replace();
            }
            else if ( DB2LoadMeta.TRUNCATE_ACTION_TRUNCATE.equals(truncateAction))
            {
                log.logBasic("run truncate table " + "TRUNCATE TABLE "+environmentSubstitute(tableName));
                data.db.execStatement("TRUNCATE TABLE "+environmentSubstitute(tableName));


            }
            else if ( DB2LoadMeta.TRUNCATE_ACTION_DELETE.equals(truncateAction))
            {
                log.logBasic("run truncate table " + "DELETE FROM "+environmentSubstitute(tableName));
                data.db.execStatement("DELETE FROM "+environmentSubstitute(tableName));


            }
            else if ( DB2LoadMeta.TRUNCATE_ACTION_CUSTOM.equals(truncateAction))
            {
                log.logBasic("run truncate table " + environmentSubstitute(meta.getTruncateSQL()));
                data.db.execStatement(environmentSubstitute(meta.getTruncateSQL()));


            }
            else
            {
                log.logBasic("run truncate table default");
                replace();
            }

        }
    }

    private void replace() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("db2 load from /dev/null of del replace into ").append(meta.getTableName());
        logBasic("satrt exec : " + sb.toString());
        String truncateTableCmd = environmentSubstitute(sb.toString());
        Process truncateTableProcess = rt.exec(truncateTableCmd);

        StreamLogger errorLogger = new StreamLogger(log, truncateTableProcess.getErrorStream(), "truncateTableError");
        StreamLogger outputLogger = new StreamLogger(log, truncateTableProcess.getInputStream(), "truncateTableOuptut");
        new Thread(errorLogger).start();
        new Thread(outputLogger).start();
        int result = truncateTableProcess.waitFor();
        if (result != 0) {
            throw new Exception("Return code " + result + " received from statement : " + truncateTableCmd);
        }
    }

    /**
     * excute After And  Befor sql
     */
    private void insertSql(String sql) throws Exception {

        if(!StringUtil.isEmpty(sql)){

            if(sql.endsWith(";")){
                sql = sql.substring(0,sql.length()-1);
            }

            if (!StringUtil.isEmpty(meta.getSchemaName())) {

                String setSchemaCmd = "db2 SET SCHEMA '" + environmentSubstitute(meta.getSchemaName()) + "'";
                log.logBasic("start exec cmd "+setSchemaCmd);
                Process setSchemProcess = rt.exec(setSchemaCmd);

                StreamLogger errorLogger = new StreamLogger(log, setSchemProcess.getErrorStream(), "truncateTableError");
                StreamLogger outputLogger = new StreamLogger(log, setSchemProcess.getInputStream(), "truncateTableOuptut");
                new Thread(errorLogger).start();
                new Thread(outputLogger).start();
                int result = setSchemProcess.waitFor();
                if (result != 0) {
                    throw new Exception("Return code " + result + " received from statement : " + setSchemProcess);
                }
            }

            //String insertCmd = "db2 " + sql +"" ;

            String insertCmd = "db2 " + sql ;

            log.logBasic(insertCmd);
            Process insertProcess = rt.exec(insertCmd);

            StreamLogger errorLogger = new StreamLogger(log, insertProcess.getErrorStream(), "truncateTableError");
            StreamLogger outputLogger = new StreamLogger(log, insertProcess.getInputStream(), "truncateTableOuptut");
            new Thread(errorLogger).start();
            new Thread(outputLogger).start();
            int result = insertProcess.waitFor();
            if (result != 0) {
                throw new Exception("Return code " + result + " received from statement : " + insertCmd);
            }
        }
    }


    private void executeLoadShell() throws Exception {


        /**
         * syntax rules :load from FILENAME of del modified by codepage=1386 coldel, chardel' dumpfile=PATH/FILENAME messages PATH/FILENAME
         * insert into TABLENAME for exception TABLENAME ALLOW READ ACCESS
         */
        StringBuffer sb = new StringBuffer();


        String logFile = environmentSubstitute(meta.getLogFile());
//        String discardFile = environmentSubstitute(meta.getDiscardFile());

        sb.append("db2 load client from ").append(data.fifoFilename).append(" of del");

        //codepage default 1386 coldel default , chardel "
        if(Const.isEmpty(meta.getPageCode())){
            sb.append(" modified by codepage=1386");
        }else{
            sb.append(" modified by codepage="+meta.getPageCode());
        }

       /* if(!StringUtil.isEmpty(discardFile)){
            createParentFolder(discardFile);
            sb. append("modified by dumpfile=").append(discardFile);
        }*/

//         sb.append(" warningcount 100");

        if(!StringUtil.isEmpty(logFile)){
            createParentFolder(logFile);
            sb.append(" messages ").append(logFile);
        }


        sb.append("  insert into ").append(meta.getTableName());
        if(!StringUtil.isEmpty(data.fieldTables)){
            sb.append("(").append(data.fieldTables).append(")");
        }

        sb.append(" NONRECOVERABLE");
        String loadCmd = environmentSubstitute(sb.toString());
        logBasic("satrt exec : " + loadCmd);


        Process loadProcess = rt.exec(loadCmd);
        StreamLogger errorLogger = new StreamLogger(log, loadProcess.getErrorStream(), "loadError");
        StreamLogger outputLogger = new StreamLogger(log, loadProcess.getInputStream(), "loadOuptut");
        new Thread(errorLogger).start();
        new Thread(outputLogger).start();
        int result = loadProcess.waitFor();
        if (result != 0) {
            throw new Exception("Return code " + result + " received from statement : db2 load ");
        }
        String sql = environmentSubstitute(meta.getAfterSQL());
        try {
            insertSql(sql);
        }catch (Exception e){
            getTrans().setVariable(Const.KETTLE_TRANS_PAN_JVM_EXIT_CODE, "12");
            logError(Messages.getString("DB2Load.ExecError.BeforSQL",e.getMessage()));
            setErrors(1L);
            stopAll();
            throw e;
        }


        disconnect();
        db2LoadIsEnd = true;
    }

    private void disconnect() throws Exception {
        String disconnectDb2Cmd =  "db2 connect reset";
        Process disconnectDb2Process = rt.exec(disconnectDb2Cmd);
        StreamLogger errorLogger = new StreamLogger(log, disconnectDb2Process.getErrorStream(), "disconnectError");
        StreamLogger outputLogger = new StreamLogger(log, disconnectDb2Process.getInputStream(), "disconnectOuptut");
        new Thread(errorLogger).start();
        new Thread(outputLogger).start();
        int result = disconnectDb2Process.waitFor();
        if (result != 0) {
            throw new KettleDatabaseException("Return code " + result + " received from statement : db2 connect reset");
        }
    }

    private void closeFile() {
        try {
            if(data.fifoWriter != null){
                data.fifoWriter.close();
                data.fifoWriter=null;
            }
            if(data.sqlRunner != null){
                data.sqlRunner.join();
                SqlRunner sqlRunner = data.sqlRunner;
                sqlRunner.checkExcn();
                data.sqlRunner = null;
            }

        } catch (Exception e) {
            logError("Exception trying to close file: " + e.toString());
            e.printStackTrace();
            setErrors(1);
        }
    }

    private void removeFile() {

        closeFile();
        try {
            if (data.fifoFilename!=null && getCopy() ==0) {
                getTrans().cmds.add("rm -f "+data.fifoFilename);
            }
        } catch(Exception e) {
            logError("Unable to delete FIFO file : "+data.fifoFilename, e);
        }
    }

    private void createParentFolder(String filename) throws Exception
    {
        // Check for parent folder
        FileObject parentfolder=null;
        try
        {
            // Get parent folder
            parentfolder=KettleVFS.getFileObject(filename).getParent();
            if(parentfolder.exists()){
                if (isDetailed()) logDetailed(BaseMessages.getString(PKG, "TextFileOutput.Log.ParentFolderExist",parentfolder.getName()));
            }else{
                if (isDetailed()) logDetailed(BaseMessages.getString(PKG, "TextFileOutput.Log.ParentFolderNotExist",parentfolder.getName()));
                //if(meta.isCreateParentFolder()){
                parentfolder.createFolder();
                if (isDetailed()) logDetailed(BaseMessages.getString(PKG, "TextFileOutput.Log.ParentFolderCreated",parentfolder.getName()));
                //}else{
                // throw new KettleException(BaseMessages.getString(PKG, "TextFileOutput.Log.ParentFolderNotExistCreateIt",parentfolder.getName(), filename));
                // }
            }
        } finally {
            if ( parentfolder != null ){
                try  {
                    parentfolder.close();
                }
                catch ( Exception ex ) {};
            }
        }
    }

}
