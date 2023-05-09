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
	String defaultDigitalsStr = "%.,";

	public InfoExtractor(StepMeta stepMeta, StepDataInterface stepDataInterface,
						 int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);

		meta = (InfoExtractorMeta) getStepMeta().getStepMetaInterface();
		data = (InfoExtractorData) stepDataInterface;
	}

	public  RowMetaAndData buildRow(InfoExtractorMeta meta,
									InfoExtractorData data, List<CheckResultInterface> remarks) {
		RowMetaInterface rowMeta = new RowMeta();
		Object[] rowData = new Object[1];

		int valtype = ValueMeta.getType("String");
		if (meta.getResultField()!= null) {
			ValueMetaInterface value = new ValueMeta(
					meta.getResultField(), valtype); // build a value!
			value.setLength(-1);
			rowMeta.addValueMeta(value);
		}
		return new RowMetaAndData(rowMeta, rowData);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {
		Object[] r = null;
		r = getRow();
		InfoExtractorMeta infoExtractorMeta = (InfoExtractorMeta)smi;
		InfoExtractorData infoExtractorData = (InfoExtractorData)sdi;
		
		String contentField = infoExtractorMeta.getContentField();
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
			if(meta.isRemoveHtml())
				content = this.removeHTMLTags(this.removeSpecifiedTags(content,"script"));
			if(meta.isRemoveBlank())
				content = this.removeBlankSpace(content);
			if(meta.isRemoveCRLF())
				content = this.removeCRLF(content);
			if(meta.isRemoveControlChars())
				content = this.removeControlChars(content);
			//String DigitalDef = infoExtractorMeta.getDefinedDigitals();
			int infoType = infoExtractorMeta.getInfoType();
			if (data.firstRow) {
				data.firstRow = false;
				data.outputMeta = getInputRowMeta().clone();
				RowMetaInterface constants = data.constants.getRowMeta();
				data.outputMeta.mergeRowMeta(constants);
			}
			String scope = null;

			String value =  null;
			if(meta.getExtractType()==InfoExtractorMeta.EXTRACT_TYPE_KEYWORD) {
				String keyWord = infoExtractorMeta.getKeyWord();
				String position = "A";// after key word
				int order = 1;// the first occurs
				int minDigitals = 1;
				int maxDigitals = 1000;
				if(keyWord==null || keyWord.length()==0)
					scope = content;
				else
				{
					int index = content.indexOf(keyWord);
					if(index>-1)
						scope = content.substring(index);
					else
						scope = content;
				}
	//			if(infoType == InfoExtractorMeta.INFO_TYPE_EMAIL)
	//				value = extractEmail(scope,keyWord,position,order,minDigitals,maxDigitals,defaultDigitalsStr);
				if (infoType == InfoExtractorMeta.INFO_TYPE_NUMBER)
					value = extractNumber(scope, keyWord, position, order, minDigitals, maxDigitals, defaultDigitalsStr);
				else if(infoType == InfoExtractorMeta.INFO_TYPE_TEXT)
					value = extractText(scope,keyWord,position,new char[]{'\r','\n'});
				else if(infoType == InfoExtractorMeta.INFO_TYPE_DATE)
					value = extractDate(scope,keyWord,position,order);
			}else if(meta.getExtractType()==InfoExtractorMeta.EXTRACT_TYPE_REGEXP)
				value = extractBypatternStr(scope,meta.getRegularExpression());
			else{
				if (log.isBasic())
					logBasic("Unsupported extract type");
				return false;
			}
			r = RowDataUtil.addRowData(r, getInputRowMeta().size(),new Object[]{value});
			
			putRow(data.outputMeta, r);
		}else
		{
			String value=null;//if the content field is not string, result is null
			r = RowDataUtil.addRowData(r, getInputRowMeta().size(),new Object[]{value});
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
	 * @param minLength: minLength of the number to be extracted
	 * @param digitalDef: digitalDef, can be null for default
	 * @return
	 * @throws KettleException 
	 */
	private String extractNumber(String scope, String keyWord, String position, int order,
			int minLength, int maxLength, String digitalDef) throws KettleException {
		int index = scope.indexOf(keyWord);
		if( index == -1)
			return null; //keyword not found, return null;
		String content;
		if(position.equalsIgnoreCase("A")||position.equalsIgnoreCase("After"))
		 	content = scope.substring(index);
		else if (position.equalsIgnoreCase("B")||position.equalsIgnoreCase("Before"))
			content = scope.substring(0,index);
		else
			throw new KettleException("unknown position:" + position);
		StringBuilder stringBuilder = new StringBuilder();
		for (int i=0;i<content.length();i++) {
			char c = content.charAt(i);
			if(isNumber(c,digitalDef.toCharArray()))
				stringBuilder.append(c);
			else if(stringBuilder.length()>0)
			{
				break;
			}
		}
		return stringBuilder.toString();
	}

	private String extractDate(String scope, String keyWord, String position, int order) throws KettleException {
		int index = scope.indexOf(keyWord);
		if( index == -1)
			return null; //keyword not found, return null;
		String content;
		if(position.equalsIgnoreCase("A")||position.equalsIgnoreCase("After"))
			content = scope.substring(index);
		else if (position.equalsIgnoreCase("B")||position.equalsIgnoreCase("Before"))
			content = scope.substring(0,index);
		else
			throw new KettleException("unknown position:" + position);
		String yearMonth = null;
		String[] patternStr = new String[]{
				"[0-9|\\s]{2,}年[0-9|\\s]*月[0-9|\\s]*日",
				"[0-9|\\s]{2,}[-|/][0-9|\\s]*[-|/][0-9|\\s]{2,}",
				"[0-9|\\s]{2,}年[0-9|\\s]*月",
				"[\\u96f6|\\u3007|\\u25CB|\\u4e00|\\u4e8c|\\u4e09|\\u56db|\\u4e94|\\u516d|\\u4e03|\\u516b|\\u4e5d|\\s]{2,}年[\\u96f6|\\u3007|\\u25CB|\\u4e00|\\u4e8c|\\u4e09|\\u56db|\\u4e94|\\u516d|\\u4e03|\\u516b|\\u4e5d|\\u5341||\\s]*月",
				"[\\u96f6|\\u3007|\\u25CB|\\u4e00|\\u4e8c|\\u4e09|\\u56db|\\u4e94|\\u516d|\\u4e03|\\u516b|\\u4e5d|\\s]{2,}年",
				"[a-z|A-Z|\\s]{2,}年[a-z|A-Z|\\\\s]*月",
				"[0-9|\\s]{2,}年",
				"[a-z|A-Z|\\s]{2,}年"
		};

		for(int i =0 ;i<patternStr.length;i++)
		{
			yearMonth= find(patternStr[i],content);
			if(yearMonth!=null)
			{
				break;
			}
		}
		return yearMonth;
		//return initDate(yearMonth);
	}

	private String extractBypatternStr(String scope, String patternStr) throws KettleException {
		String result = null;
		result= find(patternStr,scope);
		return result;
	}

	private String extractText(String scope, String keyWord, String position, char[] stopCharacters) throws KettleException {
		int index = scope.indexOf(keyWord);
		if( index == -1)
			return null; //keyword not found, return null;
		String content;
		if(position.equalsIgnoreCase("A")||position.equalsIgnoreCase("After"))
			content = scope.substring(index+keyWord.length());
		else if (position.equalsIgnoreCase("B")||position.equalsIgnoreCase("Before"))
			content = scope.substring(0,index);
		else
			throw new KettleException("unknown position:" + position);
		StringBuilder stringBuilder = new StringBuilder();
		for (int i=0;i<content.length();i++) {
			char c = content.charAt(i);
			if(!isCharacter(c,stopCharacters))
				stringBuilder.append(c);
			else if(stringBuilder.length()>0)
			{
				break;
			}
		}
		return stringBuilder.toString();
	}
	private String initDate(String yearMonth) {
		char[] chars = yearMonth.toCharArray();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			switch (c) {
				case '\u25CB':
				case '\u96f6':
				case '\u3007':
				case '0':
					sb.append('0');
					break;
				case '\u4e00':
				case '1':
					sb.append('1');
					break;
				case '\u4e8c':
				case '2':
					sb.append('2');
					break;
				case '\u4e09':
				case '3':
					sb.append('3');
					break;
				case '\u56db':
				case '4':
					sb.append('4');
					break;
				case '\u4e94':
				case '5':
					sb.append('5');
					break;
				case '\u516d':
				case '6':
					sb.append('6');
					break;
				case '\u4e03':
				case '7':
					sb.append('7');
					break;
				case '\u516b':
				case '8':
					sb.append('8');
					break;
				case '\u4e5d':
				case '9':
					sb.append('9');
					break;
				case '\u5e74':
					sb.append('\u5e74');
					break;
				case '\u6708':
					sb.append('\u6708');
					break;
				case '\u5341':
					sb.append('1').append('0');
					break;
				case ' ':
					break;
				default:
					sb.append(c);
					break;
			}
		}
		yearMonth = sb.toString();
		return yearMonth;
	}


	private boolean isNumber(char c, char[] chars) {
		if( 48 <= c && c<= 57 )
			return true;
		else {
			for(int i=0;i<chars.length;i++)
			{
				if(chars[i]==c)
					return true;
			}
		}
		return false;
	}
	private boolean isCharacter(char c, char[] chars) {
		for(int i=0;i<chars.length;i++)
		{
			if(chars[i]==c)
				return true;
		}
		return false;
	}
	/**
	 * extract report's first writer's email
	 * @param scope
	 * @param keyWord
	 * @return
	 */
	public String extractEmail(String scope, String keyWord, String position, int order,String EmailPatternDef)
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
		String matched = find(patternStr,scope);
		if(matched == null)
			return null; 
		else
		    return matched;
	}
	
	/**
	 * found the first matched strings
	 * @param patternStr1
	 * @param content
	 * @return
	 */
	private String find(String patternStr1,String content)
	{
		String result = null;
		Pattern pattern = Pattern.compile(patternStr1);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find())
		{
			result = content.substring(matcher.start(),matcher.end());
		}else
		{
			result = null;
		}
		return result;
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
				digitalChars = digitalDef.split(";");
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


	public  String removeHTMLTags(String htmlContent) {
		if(htmlContent==null)
			return null;
		StringBuffer result = new StringBuffer();
		char[] array = htmlContent.toCharArray();
		//boolean tag = false;
		int level = 0;
		for (int i = 0; i < array.length; i++) {
			switch (array[i]) {
				case '<':
					level++;
					break;
				case '>':
					level--;
					break;
				default:
					if (level == 0) {
						result.append(array[i]);
					}
					break;
			}
		}
		return result.toString().replaceAll("&nbsp;", " ").trim();
	}

	/**
	 * remove specified tag and content in the tag from a html string
	 * @param htmlContent
	 * @param tagToRemove
	 * @return
	 */
	private String removeSpecifiedTags(String htmlContent, String tagToRemove) {
		if(htmlContent==null)
			return null;
		int startIndex = htmlContent.indexOf("<"+tagToRemove);
		if(startIndex==-1)//not found tag in the content
			return htmlContent;
		else {
			String endTag = "</"+tagToRemove+">";
			int endIndex = htmlContent.indexOf(endTag);
			int tagLength = endTag.length();
			return htmlContent.substring(0,startIndex)+removeSpecifiedTags(htmlContent.substring(endIndex+tagLength,htmlContent.length()),tagToRemove);
		}
	}

	private String removeControlChars(String str)
	{
		if(str==null)
			return null;
		char[] result = new char[str.length()];
		int count=0;
		//ArrayList<Char> result = new ArrayList();
		for(int index =0;index<str.length();index++)
		{
			char c = str.charAt(index);
			if((c<32 || c==127)&&(c!=10 ||c!=13 ) ) //control chars except CRLF
				continue;
			else
				result[count++]=c;
		}
		return new String(result).substring(0,count);

	}
	private String removeCRLF(String str)
	{
		if(str==null)
			return null;
		char[] result = new char[str.length()];
		int count=0;
		for(int index =0;index<str.length();index++)
		{
			char c = str.charAt(index);
			if(c!=10 && c!=13 )
				result[count++]=c;
		}
		return new String(result).substring(0,count);

	}

	private String removeBlankSpace(String str)
	{
		if(str==null)
			return null;
		char[] result = new char[str.length()];
		int count=0;
		//ArrayList<Char> result = new ArrayList();
		for(int index =0;index<str.length();index++)
		{
			char c = str.charAt(index);
			if(c=='\u0020' || c=='\u00A0' || c=='\u3000' )//A0 is "nbsp"
				continue;
			else
				result[count++]=c;
		}
		return new String(result).substring(0,count);

	}
}
