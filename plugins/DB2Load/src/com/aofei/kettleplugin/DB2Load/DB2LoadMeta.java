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

import java.util.List;
import java.util.Map;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * Created on Aug-2017
 * 
 * @author Chu
 */
public class DB2LoadMeta extends BaseStepMeta implements StepMetaInterface {
	private static Class<?> PKG = DB2LoadMeta.class; // for i18n purposes,
														// needed by
														// Translator2!!
														// $NON-NLS-1$

	/** database connection */
	private DatabaseMeta databaseMeta;
	/** what's the schema for the target? */
	private String schemaName;

	/** what's the table for the target? */
	private String tableName;

	/** Path to the sqlldr utility */
	private String db2ClientDir;

	/** Path to the data file */
	private String dataFile;

	/** Path to the log file */
	private String logFile;

	/** Path to the bad file */
	private String badFile;

	/** Path to the discard file */
	private String discardFile;

	/** Field value to dateMask after lookup */
	private String fieldTable[];

	/** Field name in the stream */
	private String fieldStream[];

	/** boolean indicating if field needs to be updated */
	private String dateMask[];

	/** Load action */
	private String loadAction;



	/** Truncate Action */
	private String truncateAction;

	/** Truncate SQL */
	private String truncateSQL;

	private String pageCode;

	private String beforeSQL;

	private String afterSQL;
	private String databaseName;

//	private String userName;
//
//	private String password;

	/*
	 * Do not translate following values!!! They are will end up in the job
	 * export.
	 */
	final static public String ACTION_APPEND = "APPEND";
	final static public String ACTION_REPLACE = "REPLACE";
	final static public String ACTION_TRUNCATE = "TRUNCATE";

	/*
	 * Do not translate following values!!! They are will end up in the job
	 * export.
	 */
	final static public String DATE_MASK_DATE = "DATE";
	final static public String DATE_MASK_DATETIME = "DATETIME";
	
	public DB2LoadMeta() {
		super();
	}

	/**
	 * @return Returns the database.
	 */
	public DatabaseMeta getDatabaseMeta() {
		return databaseMeta;
	}

	/**
	 * @param database
	 *            The database to set.
	 */
	public void setDatabaseMeta(DatabaseMeta database) {
		this.databaseMeta = database;
	}

	public String getTruncateAction() {
		return truncateAction;
	}

	public void setTruncateAction(String truncateAction) {
		this.truncateAction = truncateAction;
	}

	public String getTruncateSQL() {
		return truncateSQL;
	}

	public void setTruncateSQL(String truncateSQL) {
		this.truncateSQL = truncateSQL;
	}


	/**
	 * @return Returns the tableName.
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName
	 *            The tableName to set.
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getDB2ClientDir() {
		return db2ClientDir;
	}

	public void setDB2ClientDir(String sqlldr) {
		this.db2ClientDir = sqlldr;
	}

	/**
	 * @return Returns the fieldTable.
	 */
	public String[] getFieldTable() {
		return fieldTable;
	}

	/**
	 * @param fieldTable
	 *            The fieldTable to set.
	 */
	public void setFieldTable(String[] updateLookup) {
		this.fieldTable = updateLookup;
	}

	/**
	 * @return Returns the fieldStream.
	 */
	public String[] getFieldStream() {
		return fieldStream;
	}

	/**
	 * @param fieldStream
	 *            The fieldStream to set.
	 */
	public void setFieldStream(String[] updateStream) {
		this.fieldStream = updateStream;
	}

	public String[] getDateMask() {
		return dateMask;
	}

	public void setDateMask(String[] dateMask) {
		this.dateMask = dateMask;
	}

	public String getPageCode() {
		return pageCode;
	}

	public void setPageCode(String pageCode) {
		this.pageCode = pageCode;
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode, databases);
	}

	public void allocate(int nrvalues) {
		fieldTable = new String[nrvalues];
		fieldStream = new String[nrvalues];
		dateMask = new String[nrvalues];
	}

	public Object clone() {
		DB2LoadMeta retval = (DB2LoadMeta) super.clone();
		int nrvalues = fieldTable.length;

		retval.allocate(nrvalues);

		for (int i = 0; i < nrvalues; i++) {
			retval.fieldTable[i] = fieldTable[i];
			retval.fieldStream[i] = fieldStream[i];
			retval.dateMask[i] = dateMask[i];
		}
		return retval;
	}

	private void readData(Node stepnode,
			List<? extends SharedObjectInterface> databases)
			throws KettleXMLException {
		try {

			String con     = XMLHandler.getTagValue(stepnode, "connection");
			databaseMeta   = DatabaseMeta.findDatabase(databases, con);
			databaseName = XMLHandler.getTagValue(stepnode, "database_name"); //$NON-NLS-1$
//			userName = XMLHandler.getTagValue(stepnode, "user_name"); //$NON-NLS-1$
//			password = XMLHandler.getTagValue(stepnode, "password"); //$NON-NLS-1$		
			schemaName = XMLHandler.getTagValue(stepnode, "schema"); //$NON-NLS-1$
			tableName = XMLHandler.getTagValue(stepnode, "table"); //$NON-NLS-1$
			
			loadAction = XMLHandler.getTagValue(stepnode, "load_action"); //$NON-NLS-1$
			truncateAction     = XMLHandler.getTagValue(stepnode, "truncate_action");  //Tony 20180807
			truncateSQL    = XMLHandler.getTagValue(stepnode, "truncate_sql"); //Tony 20180807

			db2ClientDir = XMLHandler.getTagValue(stepnode, "sqlldr"); //$NON-NLS-1$
			dataFile = XMLHandler.getTagValue(stepnode, "data_file"); //$NON-NLS-1$
			logFile = XMLHandler.getTagValue(stepnode, "log_file"); //$NON-NLS-1$
			badFile = XMLHandler.getTagValue(stepnode, "bad_file"); //$NON-NLS-1$
			discardFile = XMLHandler.getTagValue(stepnode, "discard_file"); //$NON-NLS-1$

			beforeSQL = XMLHandler.getTagValue(stepnode, "before_sql"); //$NON-NLS-1$
			afterSQL = XMLHandler.getTagValue(stepnode, "after_sql"); //$NON-NLS-1$

			pageCode = Const.NVL(XMLHandler.getTagValue(stepnode, "page_code"), "1386"); //$NON-NLS-1$

			int nrvalues = XMLHandler.countNodes(stepnode, "mapping"); //$NON-NLS-1$
			allocate(nrvalues);

			for (int i = 0; i < nrvalues; i++) {
				Node vnode = XMLHandler.getSubNodeByNr(stepnode, "mapping", i); //$NON-NLS-1$

				fieldTable[i] = XMLHandler.getTagValue(vnode, "stream_name"); //$NON-NLS-1$
				fieldStream[i] = XMLHandler.getTagValue(vnode, "field_name"); //$NON-NLS-1$
				if (fieldStream[i] == null)
					fieldStream[i] = fieldTable[i]; // default: the same name!
				String locDateMask = XMLHandler.getTagValue(vnode, "date_mask"); //$NON-NLS-1$
				if (locDateMask == null) {
					dateMask[i] = "";
				} else {
					if (DB2LoadMeta.DATE_MASK_DATE.equals(locDateMask)
							|| DB2LoadMeta.DATE_MASK_DATETIME
									.equals(locDateMask)) {
						dateMask[i] = locDateMask;
					} else {
						dateMask[i] = "";
					}
				}
			}
		} catch (Exception e) {
			throw new KettleXMLException(
					BaseMessages
							.getString(PKG,
									"OraBulkLoaderMeta.Exception.UnableToReadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault() {
		fieldTable = null;
		databaseMeta = null;
		schemaName = ""; //$NON-NLS-1$
		tableName = BaseMessages.getString(PKG, "DB2LoadMeta.DefaultTableName"); //$NON-NLS-1$
		loadAction = ACTION_APPEND;
		db2ClientDir = ""; //$NON-NLS-1$
		dataFile = ""; //$NON-NLS-1$
		logFile = ""; //$NON-NLS-1$
		badFile = ""; //$NON-NLS-1$
		discardFile = ""; //$NON-NLS-1$
		pageCode = "1386";
		int nrvalues = 0;
		allocate(nrvalues);
	}

	public String getXML() {
		StringBuffer retval = new StringBuffer(300);
		
		retval.append("    "+XMLHandler.addTagValue("connection",     databaseMeta==null?"":databaseMeta.getName()));
		retval
		.append("    ").append(XMLHandler.addTagValue("database_name", databaseName)); //$NON-NLS-1$ //$NON-NLS-2$
//		retval
//		.append("    ").append(XMLHandler.addTagValue("user_name", userName)); //$NON-NLS-1$ //$NON-NLS-2$
//		retval
//		.append("    ").append(XMLHandler.addTagValue("password", password)); //$NON-NLS-1$ //$NON-NLS-2$
		
		retval.append("    ").append(XMLHandler.addTagValue("schema", schemaName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("table", tableName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("load_action", loadAction)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    ").append(XMLHandler.addTagValue("truncate_action",  truncateAction));    //Tony 20180807
		retval.append("    ").append(XMLHandler.addTagValue("truncate_sql",  truncateSQL));    //Tony 20180807

		retval.append("    ").append(XMLHandler.addTagValue("sqlldr", db2ClientDir)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("data_file", dataFile)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("log_file", logFile)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("bad_file", badFile)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("discard_file", discardFile)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    ").append(XMLHandler.addTagValue("before_sql", beforeSQL)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("after_sql", afterSQL)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    ").append(XMLHandler.addTagValue("page_code", pageCode)); //$NON-NLS-1$ //$NON-NLS-2$

		for (int i = 0; i < fieldTable.length; i++) {
			retval.append("      <mapping>").append(Const.CR); //$NON-NLS-1$
			retval
					.append("        ").append(XMLHandler.addTagValue("stream_name", fieldTable[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval
					.append("        ").append(XMLHandler.addTagValue("field_name", fieldStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval
					.append("        ").append(XMLHandler.addTagValue("date_mask", dateMask[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </mapping>").append(Const.CR); //$NON-NLS-1$
		}

		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step,
			List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {
		try {
			databaseMeta = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);  //$NON-NLS-1$
			schemaName = rep.getStepAttributeString(id_step, "schema"); //$NON-NLS-1$
			tableName = rep.getStepAttributeString(id_step, "table"); //$NON-NLS-1$
			loadAction = rep.getStepAttributeString(id_step, "load_action"); //$NON-NLS-1$

			truncateAction     =      rep.getStepAttributeString(id_step,  "truncate_action");    //Tony 20180807
			truncateSQL     =      rep.getStepAttributeString(id_step,  "truncate_sql");   //Tony 20180807

			db2ClientDir = rep.getStepAttributeString(id_step, "sqlldr"); //$NON-NLS-1$			
			dataFile = rep.getStepAttributeString(id_step, "data_file"); //$NON-NLS-1$
			logFile = rep.getStepAttributeString(id_step, "log_file"); //$NON-NLS-1$
			badFile = rep.getStepAttributeString(id_step, "bad_file"); //$NON-NLS-1$
			discardFile = rep.getStepAttributeString(id_step, "discard_file"); //$NON-NLS-1$

			databaseName = rep.getStepAttributeString(id_step, "database_name"); //$NON-NLS-1$
//			userName = rep.getStepAttributeString(id_step, "user_name"); //$NON-NLS-1$
//			password = rep.getStepAttributeString(id_step, "password"); //$NON-NLS-1$
			
			beforeSQL = rep.getStepAttributeString(id_step, "before_sql"); //$NON-NLS-1$
			afterSQL = rep.getStepAttributeString(id_step, "after_sql"); //$NON-NLS-1$

			pageCode = Const.NVL(rep.getStepAttributeString(id_step, "page_code"), "1386"); //$NON-NLS-1$

			int nrvalues = rep.countNrStepAttributes(id_step, "stream_name"); //$NON-NLS-1$

			allocate(nrvalues);

			for (int i = 0; i < nrvalues; i++) {
				fieldTable[i] = rep.getStepAttributeString(id_step, i,
						"stream_name"); //$NON-NLS-1$
				fieldStream[i] = rep.getStepAttributeString(id_step, i,
						"field_name"); //$NON-NLS-1$
				dateMask[i] = rep.getStepAttributeString(id_step, i,
						"date_mask"); //$NON-NLS-1$
			}
		} catch (Exception e) {
			throw new KettleException(
					BaseMessages
							.getString(PKG,
									"DB2LoadMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation,
			ObjectId id_step) throws KettleException {
		try {
			rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
			rep.saveStepAttribute(id_transformation, id_step,
					"database_name", databaseName); //$NON-NLS-1$
//			rep.saveStepAttribute(id_transformation, id_step,
//					"user_name", userName); //$NON-NLS-1$
//			rep.saveStepAttribute(id_transformation, id_step,
//					"password", password); //$NON-NLS-1$
			
			rep.saveStepAttribute(id_transformation, id_step,
					"schema", schemaName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step,
					"table", tableName); //$NON-NLS-1$

			rep.saveStepAttribute(id_transformation, id_step,
					"load_action", loadAction); //$NON-NLS-1$

			rep.saveStepAttribute(id_transformation, id_step, "truncate_action",     truncateAction);     //Tony 20180807
			rep.saveStepAttribute(id_transformation, id_step, "truncate_sql",     truncateSQL);     //Tony 20180807

			rep.saveStepAttribute(id_transformation, id_step, "sqlldr", db2ClientDir); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step,
					"data_file", dataFile); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step,
					"log_file", logFile); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step,
					"bad_file", badFile); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step,
					"discard_file", discardFile); //$NON-NLS-1$

			rep.saveStepAttribute(id_transformation, id_step,
					"before_sql", beforeSQL); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step,
					"after_sql", afterSQL); //$NON-NLS-1$

			rep.saveStepAttribute(id_transformation, id_step,
					"page_code", pageCode); //$NON-NLS-1$

			for (int i = 0; i < fieldTable.length; i++) {
				rep.saveStepAttribute(id_transformation, id_step, i,
						"stream_name", fieldTable[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i,
						"field_name", fieldStream[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i,
						"date_mask", dateMask[i]); //$NON-NLS-1$
			}

			// Also, save the step-database relationship!
			if (databaseMeta != null)
				rep.insertStepDatabase(id_transformation, id_step, databaseMeta
						.getObjectId());
		} catch (Exception e) {
			throw new KettleException(
					BaseMessages
							.getString(PKG,
									"DB2LoadMeta.Exception.UnableToSaveStepInfoToRepository") + id_step, e); //$NON-NLS-1$
		}
	}

	public void getFields(RowMetaInterface rowMeta, String origin,
			RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
			throws KettleStepException {
		// Default: nothing changes to rowMeta
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev, String input[],
			String output[], RowMetaInterface info) {
		CheckResult cr;
		boolean flag = true;
		if (databaseMeta != null)
		{
			if(Const.isEmpty(databaseName)){
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "Database alias is empty", stepMeta);
				remarks.add(cr);
				flag = false;
			}else{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "Database alias is ok", stepMeta);
				remarks.add(cr);
			}
			if(Const.isEmpty(tableName)){
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "Target tableName is empty", stepMeta);
				remarks.add(cr);
				flag = false;
			}else{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "Target tableName is ok", stepMeta);
				remarks.add(cr);
			}
			//check connection
			if(flag){
				try {
					 Runtime rt = Runtime.getRuntime();
					 StringBuffer sb = new StringBuffer();
					 String pass = transMeta.environmentSubstitute(databaseMeta.getPassword());
				     pass =  Encr.decryptPasswordOptionallyEncrypted(pass);
				     String user = transMeta.environmentSubstitute(databaseMeta.getUsername());
				     //alias
				     databaseName = transMeta.environmentSubstitute(databaseName);
				     sb.append("db2 connect to  ").append(databaseName).append(" user ").append(user).append(" using ").append("'"+pass+"'");
				     String db2ConnectCmd = transMeta.environmentSubstitute(sb.toString());
				     log.logBasic("db2ConnectCmd : " +db2ConnectCmd);
				     Process db2ConnectProcess = rt.exec(db2ConnectCmd);
				     int result = db2ConnectProcess.waitFor();
				     if (result != 0) {
				    	 cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "Return code " + result + " received from statement : db2 connect to ["+databaseName+"]", stepMeta);
//				    	 remarks.add(cr);
				     }else{
				    	 cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "db2 connect to ["+databaseName+"] successful", stepMeta);
				    	 remarks.add(cr);
				     }
				} catch (Exception e) {
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, e.getMessage(), stepMeta);
//			    	remarks.add(cr);
				}
			}
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "Connection is not exists ", stepMeta);
			remarks.add(cr);
		}
		
		//check file path
		if(Const.isEmpty(this.logFile)){
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "logFile is empty", stepMeta);
			remarks.add(cr);
			flag = false;
		}else{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "logFile is ok", stepMeta);
			remarks.add(cr);
		}
		if(Const.isEmpty(this.badFile)){
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "badFile is empty", stepMeta);
			remarks.add(cr);
			flag = false;
		}else{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "badFile is ok", stepMeta);
			remarks.add(cr);
		}
		if(Const.isEmpty(this.discardFile)){
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "discardFile is empty", stepMeta);
			remarks.add(cr);
			flag = false;
		}else{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "discardFile is ok", stepMeta);
			remarks.add(cr);
		}
		// See if we have input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(
					CheckResultInterface.TYPE_RESULT_OK,
					BaseMessages
							.getString(PKG,
									"DB2LoadMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"DB2LoadMeta.CheckResult.NoInputError"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public SQLStatement getSQLStatements(TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev)
			throws KettleStepException {
		SQLStatement retval = new SQLStatement(stepMeta.getName(),
				databaseMeta, null); // default: nothing to do!

		if (databaseMeta != null) {
			if (prev != null && prev.size() > 0) {
				// Copy the row
				RowMetaInterface tableFields = new RowMeta();

				// Now change the field names
				for (int i = 0; i < fieldTable.length; i++) {
					ValueMetaInterface v = prev.searchValueMeta(fieldStream[i]);
					if (v != null) {
						ValueMetaInterface tableField = v.clone();
						tableField.setName(fieldTable[i]);
						tableFields.addValueMeta(tableField);
					} else {
						throw new KettleStepException("Unable to find field ["
								+ fieldStream[i] + "] in the input rows");
					}
				}

				if (!Const.isEmpty(tableName)) {
					Database db = new Database(loggingObject, databaseMeta);
					db.shareVariablesWith(transMeta);
					try {
						db.connect();

						String schemaTable = databaseMeta
								.getQuotedSchemaTableCombination(
										transMeta
												.environmentSubstitute(schemaName),
										transMeta
												.environmentSubstitute(tableName));
						String sql = db.getDDL(schemaTable, tableFields, null,
								false, null, true);

						if (sql.length() == 0)
							retval.setSQL(null);
						else
							retval.setSQL(sql);
					} catch (KettleException e) {
						retval
								.setError(BaseMessages.getString(PKG,
										"DB2LoadMeta.GetSQL.ErrorOccurred") + e.getMessage()); //$NON-NLS-1$
					}
				} else {
					retval.setError(BaseMessages.getString(PKG,
							"DB2LoadMeta.GetSQL.NoTableDefinedOnConnection")); //$NON-NLS-1$
				}
			} else {
				retval.setError(BaseMessages.getString(PKG,
						"DB2LoadMeta.GetSQL.NotReceivingAnyFields")); //$NON-NLS-1$
			}
		} else {
			retval.setError(BaseMessages.getString(PKG,
					"DB2LoadMeta.GetSQL.NoConnectionDefined")); //$NON-NLS-1$
		}

		return retval;
	}

	public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev, String input[],
			String output[], RowMetaInterface info) throws KettleStepException {
		if (prev != null) {
			/* DEBUG CHECK THIS */
			// Insert dateMask fields : read/write
			for (int i = 0; i < fieldTable.length; i++) {
				ValueMetaInterface v = prev.searchValueMeta(fieldStream[i]);

				DatabaseImpact ii = new DatabaseImpact(
						DatabaseImpact.TYPE_IMPACT_READ_WRITE,
						transMeta.getName(),
						stepMeta.getName(),
						databaseMeta.getDatabaseName(),
						transMeta.environmentSubstitute(tableName),
						fieldTable[i],
						fieldStream[i],
						v != null ? v.getOrigin() : "?", "", "Type = " + v.toStringMeta()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				impact.add(ii);
			}
		}
	}

	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
			Trans trans) {
		return new DB2Load(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData() {
		return new DB2LoadData();
	}

	public DatabaseMeta[] getUsedDatabaseConnections() {
		if (databaseMeta != null) {
			return new DatabaseMeta[] { databaseMeta };
		} else {
			return super.getUsedDatabaseConnections();
		}
	}

	public RowMetaInterface getRequiredFields(VariableSpace space)
			throws KettleException {
		String realTableName = space.environmentSubstitute(tableName);
		String realSchemaName = space.environmentSubstitute(schemaName);

		if (databaseMeta != null) {
			Database db = new Database(loggingObject, databaseMeta);
			try {
				db.connect();

				if (!Const.isEmpty(realTableName)) {
					String schemaTable = databaseMeta
							.getQuotedSchemaTableCombination(realSchemaName,
									realTableName);

					// Check if this table exists...
					if (db.checkTableExists(schemaTable)) {
						return db.getTableFields(schemaTable);
					} else {
						throw new KettleException(BaseMessages.getString(PKG,
								"DB2LoadMeta.Exception.TableNotFound"));
					}
				} else {
					throw new KettleException(BaseMessages.getString(PKG,
							"DB2LoadMeta.Exception.TableNotSpecified"));
				}
			} catch (Exception e) {
				throw new KettleException(BaseMessages.getString(PKG,
						"DB2LoadMeta.Exception.ErrorGettingFields"), e);
			} finally {
				db.disconnect();
			}
		} else {
			throw new KettleException(BaseMessages.getString(PKG,
					"DB2LoadMeta.Exception.ConnectionNotDefined"));
		}

	}

	/**
	 * @return the schemaName
	 */
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * @param schemaName
	 *            the schemaName to set
	 */
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getBadFile() {
		return badFile;
	}

	public void setBadFile(String badFile) {
		this.badFile = badFile;
	}

	public String getDataFile() {
		return dataFile;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	public String getDiscardFile() {
		return discardFile;
	}

	public void setDiscardFile(String discardFile) {
		this.discardFile = discardFile;
	}

	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	public void setLoadAction(String action) {
		this.loadAction = action;
	}

	public String getLoadAction() {
		return this.loadAction;
	}

	public void setBeforSQL(String text) {
		beforeSQL = text;
	}

	public void setAfterSQL(String text) {
		afterSQL = text;
	}

	public String getBeforSQL() {
		return beforeSQL;
	}

	public String getAfterSQL() {
		return afterSQL;
	}

	public boolean isWriteSepatatorAfterLashColumn() {
		return false;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

}