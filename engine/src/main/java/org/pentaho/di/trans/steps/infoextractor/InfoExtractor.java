/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.infoextractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;



public class InfoExtractor extends BaseStep implements StepInterface {
	
	private InfoExtractorMeta meta;
	private InfoExtractorData data;
	String[] defaultDigitals = {"0","1","2","3","4","5","6","7","8","9","%",".",","};
	
	public InfoExtractor(StepMeta stepMeta, StepDataInterface stepDataInterface,
						 int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);

		meta = (InfoExtractorMeta) getStepMeta().getStepMetaInterface();
		data = (InfoExtractorData) stepDataInterface;
	}

	public  RowMetaAndData buildRow(InfoExtractorMeta meta,
									InfoExtractorData data, List<CheckResultInterface> remarks) {
		RowMetaInterface rowMeta = new RowMeta();
		Object[] rowData = new Object[meta.getFieldName().length];

		for (int i = 0; i < meta.getFieldName().length; i++) {
			int valtype = ValueMeta.getType("String");
			if (meta.getFieldName()[i] != null) {
				ValueMetaInterface value = new ValueMeta(
						meta.getFieldName()[i], valtype); // build a value!
				value.setLength(-1);
				rowMeta.addValueMeta(value);
				}
			}

		return new RowMetaAndData(rowMeta, rowData);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {
		Object[] r = null;
		r = getRow();
		InfoExtractorMeta infoExtractorMeta = (InfoExtractorMeta)smi;
		InfoExtractorData infoExtractorData = (InfoExtractorData)sdi;
		
		String contentField = infoExtractorMeta.getResultField();
		if(contentField==null || contentField.length()==0)
			throw new KettleException("content field not found");
		
		
		
		if (r == null) // no more rows to be expected from the previous step(s)
		{
			setOutputDone();
			return false;
		}
		
		if (first)
        {
            first = false;
            int contentFieldIndex = this.getInputRowMeta().indexOfValue(contentField);
            infoExtractorData.setContentFieldIndex(contentFieldIndex);
        }
		
		String content = null;
		if(r[infoExtractorData.getContentFieldIndex()] instanceof String)
		{
			content = (String)r[infoExtractorData.getContentFieldIndex()];
			String contentMark = infoExtractorMeta.getContentStartMark();
			Object[] values =  new Object[infoExtractorMeta.getFieldName().length];
			for (int i = 0; i < infoExtractorMeta.getFieldName().length; i++)
			{
				String position = infoExtractorMeta.getPosition()[i];
				int order = infoExtractorMeta.getOrder()[i];
				int minDigitals = infoExtractorMeta.getMinLength()[i];
				int maxDigitals = infoExtractorMeta.getMaxLength()[i];
				String DigitalDef = infoExtractorMeta.getDefinedDigitals()[i];
				String keyWord = infoExtractorMeta.getKeyWord()[i];
				String contentMode = infoExtractorMeta.getContentMode()[i];
				
				if (data.firstRow) {
					data.firstRow = false;
					data.outputMeta = getInputRowMeta().clone();
					RowMetaInterface constants = data.constants.getRowMeta();
					data.outputMeta.mergeRowMeta(constants);
				}
				String scope = null;
				if(contentMark==null || contentMark.length()==0)
					scope = content;
				else
				{
					int index = content.indexOf(contentMark);
					if(index>-1)
						scope = content.substring(index);
					else
						scope = content; 
				}
				String value = null;
				if(Rule.getContentMode(contentMode) == Rule.CONTENT_MODE_EMAIL)
					value = extractEmail(scope,keyWord,position,order,minDigitals,maxDigitals,DigitalDef);
				else if(Rule.getContentMode(contentMode) == Rule.CONTENT_MODE_DIGITAL)
					value = extractNumber(scope,keyWord,position,order,minDigitals,maxDigitals,DigitalDef);
				
				values[i] = value;
			}
			r = RowDataUtil.addRowData(r, getInputRowMeta().size(),values);
			
			putRow(data.outputMeta, r);
		}else
		{
			Object[] values =  new Object[infoExtractorMeta.getFieldName().length];
			for (int i = 0; i < infoExtractorMeta.getFieldName().length; i++)
			{
				values[i] = null;

			}
			
			putRow(data.outputMeta, r);
		}
		
		if (log.isRowLevel()) {
			log.logRowlevel(toString(), BaseMessages.getString("NumberExtractor.Log.LineNr", Long.toString(getLinesWritten()),
					getInputRowMeta().getString(r)));
		}

		if (checkFeedback(getLinesWritten())) {
			if (log.isBasic())
					logBasic(BaseMessages.getString("NumberExtractor.Log.LineNr", Long
							.toString(getLinesWritten())));
		}
		return true;
	}

	/**
	 * extract number from a string
	 * @param scope
	 * @param keyWord: string to extract from
	 * @param position: After/Before
	 * @param order:  sequence of the number to be extracted
	 * @param minLength: minum digitals of the number to be extracted
	 * @param digitalDef: digital defitination, can be null for default 
	 * @return
	 * @throws KettleException 
	 */
	private String extractNumber(String scope, String keyWord, String position, int order,
			int minLength, int maxLength, String digitalDef) throws KettleException {
		int index = scope.indexOf(keyWord);
		if( index == -1)
			index = 0;
		if(position.equalsIgnoreCase("A")||position.equalsIgnoreCase("After"))
			return search(scope.substring(index),true,order,minLength,maxLength,digitalDef);
		else if (position.equalsIgnoreCase("B")||position.equalsIgnoreCase("Before"))
			return search(scope.substring(0,index),false,order,minLength,maxLength,digitalDef);
		else 
			throw new KettleException("unknown position:" + position);
	
	}
	
	/**
	 * extract report's first writer's email
	 * @param scope
	 * @param keyWord
	 * @return
	 */
	public String extractEmail(String scope, String keyWord, String position, int order,
			int minDigitals, int maxDigitals, String EmailPatternDef)
	{
		String result="";
		int index;
		if(keyWord==null || keyWord.length()==0)
			index = 0;
		else
			index = scope.indexOf(keyWord);
		
		if( index == -1)
			index = 0;
		
		scope = scope.substring(index);
		String patternStr = 
				"[\\.|\\-|\\_|\\w]+@((\\w|\\-)+\\.)+[a-z|A-Z]{2,3}";
		if(EmailPatternDef!=null && EmailPatternDef.length()>0)
		{
			patternStr = EmailPatternDef;
		}		
		ArrayList<String> matched = find(patternStr,scope);
		if(matched == null)
			return null; 
		for (int i=0;i<matched.size();i++)
		{
			result +=matched.get(i)+";";
		}
		
		if(result.endsWith(";"))
			result= result.substring(0,result.length()-2);
		return result;
	}
	
	/**
	 * found all matched strings
	 * @param patternStr1
	 * @param content
	 * @return
	 */
	private ArrayList<String> find(String patternStr1,String content)
	{
		ArrayList<String> results = null;
		String one = null;
		Pattern pattern = Pattern.compile(patternStr1);
		Matcher matcher = pattern.matcher(content);			
		while (matcher.find())
		{
			one = content.substring(matcher.start(),matcher.end());
			if(results==null)
				results = new ArrayList<String>();
			results.add(one);			
		}
		return results;
	}
	
/**
 * 
 * @param scope
 * @param fromBegin   true: from begin , false: from end.
 * @param order  the sequence of the number to be extracted.
 * @param minLength digitals to extract.
 * @param digitalDef    user defined digitals, can be null to use default.
 * @return
 */
	private String search(String scope, boolean fromBegin, int order,
			int minLength, int maxLength, String digitalDef) {
			String result = null;
			String[] digitalChars = null;
			
			if (digitalDef!=null && digitalDef.length()>0)
				digitalChars = digitalDef.split(" ");
			else				
				digitalChars = defaultDigitals;
			
			int begin =0;
			int end =0;
			int count =0;
			int status = 0;
			boolean found = true;
			
			if(!fromBegin)
			{
				StringBuilder reverse = new StringBuilder(scope);
				scope = new String(reverse.reverse());
			}
			
			for (int i=0;i<scope.length();i++)
			{
				char c=scope.charAt(i);
				found = false;
				int j;
				for (j=0;j<digitalChars.length;j++)
				{
					if (c == digitalChars[j].charAt(0))
					{
						found = true;
						break;
					}
				}	
				
				if (found && status==0)
				{
					status = 1;
					begin = i;
				}
				else if (!found && status==1)
				{
					status = 0;
					end = i;					
					String extractedNumber = scope.substring(begin,end);
					if ( extractedNumber.length()>= minLength)
					{
						count++;
					}
					if(count== order && extractedNumber.length()>= minLength)
					{
						result =  extractedNumber;
						break;
					}
				}
					
			}
			
			if(!fromBegin)
			{
				StringBuilder reverse = new StringBuilder(result);
				result = new String(reverse.reverse());				
			}
			
			if(result==null)
				return result;
			
			if(maxLength>0 && result.length()>=maxLength)
				return result.substring(0,maxLength);
			else 
				return result;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (InfoExtractorMeta) smi;
		data = (InfoExtractorData) sdi;
		data.firstRow = true;

		if (super.init(smi, sdi)) {
			// Create a row (constants) with all the values in it...
			List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>(); // stores the errors...
			data.constants = buildRow(meta, data, remarks);
			if (remarks.isEmpty()) {
				return true;
			} else {
				for (int i = 0; i < remarks.size(); i++) {
					CheckResultInterface cr = remarks.get(i);
					log.logError(getStepname(), cr.getText());
				}
			}
		}
		return false;
	}

	//
	// Run is were the action happens!
//	public void run() {
//		BaseStep.runStepThread(this, meta, data);
//	}

}
