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

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.TarTableDefine;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


public class DB2LoadData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;

	public  Database db;
	public int    keynrs[];         // nr of keylookup -value in row...

	public boolean oneFileOpened;


	public NumberFormat nf;
	public DecimalFormat df;
	public DecimalFormatSymbols dfs;
	public SimpleDateFormat daf;
	public DateFormatSymbols dafs;


    
    public BufferedOutputStream fifoWriter;
    public OutputStream fos;
    
    public DecimalFormat        defaultDecimalFormat;
    public DecimalFormatSymbols defaultDecimalFormatSymbols;

    public SimpleDateFormat  defaultDateFormat;
    public DateFormatSymbols defaultDateFormatSymbols;
    public Process cmdProc;

	public byte[] binarySeparator;
	public byte[] binaryEnclosure;
	public byte[] binaryNewline;


	public List<String> previouslyOpenedFiles;
	
	public int  fileNameFieldIndex;


	public Map<String,OutputStream> fileWriterMap;



	public String fifoFilename;

	public FileObject tempFile;

    public DB2Load.SqlRunner sqlRunner;
	public ValueMetaInterface[] bulkFormatMeta;

	public ValueMetaInterface bulkTimestampMeta;
	public ValueMetaInterface bulkDateMeta;
	public ValueMetaInterface bulkNumberMeta;
	public String fieldTables;
	
	public Map<String, TarTableDefine> tarTableDefine;

	/**
	 * 
	 */
	public DB2LoadData()
	{
		super();
		tarTableDefine = null;
		db = null;
		nf = NumberFormat.getInstance();
		df = (DecimalFormat)nf;
		dfs=new DecimalFormatSymbols();

		daf = new SimpleDateFormat();
		dafs= new DateFormatSymbols();
        
        defaultDecimalFormat = (DecimalFormat)NumberFormat.getInstance();
        defaultDecimalFormatSymbols =  new DecimalFormatSymbols();

        defaultDateFormat = new SimpleDateFormat();
        defaultDateFormatSymbols = new DateFormatSymbols();
        
        previouslyOpenedFiles = new ArrayList<String>();
        fileNameFieldIndex = -1;

        cmdProc = null;
        oneFileOpened=false;
        
        fileWriterMap = new HashMap<String,OutputStream>();

		binaryEnclosure = "\"".getBytes();
		binarySeparator = ",".getBytes();
		binaryNewline = "\n".getBytes();
	}


}
