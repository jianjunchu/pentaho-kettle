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

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaNone;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;


public class InfoExtractorMeta extends BaseStepMeta implements StepMetaInterface
{


	private static Class<?> PKG = InfoExtractorMeta.class;
	private  String definedDigitals[];
//	private  String decimal[];
//	private  String group[];
//	private  String value[];
	
	private  String contentField;
	//private  String contentMode;
	private  String keyWord;

	private  String regularExpression;


	private  boolean isRemoveHtml;
	private  boolean isRemoveBlank;
	private  boolean isRemoveControlChars;
	private  boolean isRemoveCRLF;

	private  int extractType;
	public  static int EXTRACT_TYPE_KEYWORD =1;
	public  static int EXTRACT_TYPE_REGEXP =2;
	private  int infoType;

	public static final int INFO_TYPE_TEXT = 1;
	public static final int INFO_TYPE_NUMBER = 2;
	public static final int INFO_TYPE_DATE = 3;

	private String resultField;
	private String contentStartMark;
	private String contentEndMark;


	//following variables will not be used by now, take as a constant. maybe useful in the future
	private  String position="A";
	private  int order=1;
	private  int minLength=1;
	private  int maxLength=1000;

	public InfoExtractorMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
    /**
     * @return Returns the currency.
     */
    public String[] getDefinedDigitals()
    {
        return definedDigitals;
    }
    
    /**
     * @param definedDigitals The currency to set.
     */
    public void setDefinedDigitals(String[] definedDigitals)
    {
        this.definedDigitals = definedDigitals;
    }
    
    /**
     * @return Returns the decimal.
     */
//    public String[] getDecimal()
//    {
//        return decimal;
//    }
//    
//    /**
//     * @param decimal The decimal to set.
//     */
//    public void setDecimal(String[] decimal)
//    {
//        this.decimal = decimal;
//    }
    
    /**
     * @return Returns the fieldFormat.
     */
    public String getPosition()
    {
        return position;
    }
    
    /**
     * @param position The fieldFormat to set.
     */
    public void setPosition(String position)
    {
        this.position = position;
    }
	public String getRegularExpression() {
		return regularExpression;
	}

	public void setRegularExpression(String regularExpression) {
		this.regularExpression = regularExpression;
	}
    /**
     * @return Returns the fieldLength.
     */
    public int getOrder()
    {
        return order;
    }
    
    /**
     * @param order The fieldLength to set.
     */
    public void setOrder(int order)
    {
        this.order = order;
    }
    
    /**
     * @return Returns the fieldName.
     */
    public String getContentField()
    {
        return contentField;
    }
    
    /**
     * @param contentField The fieldName to set.
     */
    public void setContentField(String contentField)
    {
        this.contentField = contentField;
    }
    
    /**
     * @return Returns the fieldPrecision.
     */
    public int getMinLength()
    {
        return minLength;
    }
    
    /**
     * @return Returns the fieldPrecision.
     */
    public int getMaxLength()
    {
        return maxLength;
    }
    
    /**
     * @param digitals
     */
    public void setMinLength(int digitals)
    {
        this.minLength = digitals;
    }
    
    /**
     * @param digitals
     */
    public void setMaxLength(int digitals)
    {
        this.maxLength = digitals;
    }
    /**
     * @return Returns the fieldType.
     */
    public String getKeyWord()
    {
        return keyWord;
    }
    
    /**
     * @param keyWord The fieldType to set.
     */
    public void setKeyWord(String keyWord)
    {
        this.keyWord = keyWord;
    }
    
    /**
     * @return Returns the group.
     */
//    public String[] getGroup()
//    {
//        return group;
//    }
//    
//    /**
//     * @param group The group to set.
//     */
//    public void setGroup(String[] group)
//    {
//        this.group = group;
//    }
    
    /**
     * @return Returns the value.
     */
//    public String[] getValue()
//    {
//        return value;
//    }
//    
//    /**
//     * @param value The value to set.
//     */
//    public void setValue(String[] value)
//    {
//        this.value = value;
//    }
    
    
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

//	public void allocate(int nrfields)
//	{
//		fieldName      = new String[nrfields];
//		contentMode = new String[nrfields];
//		keyWord      = new String[nrfields];
//		position    = new String[nrfields];
//		order    = new int[nrfields];
//		minLength = new int[nrfields];
//		maxLength = new int[nrfields];
//		definedDigitals       = new String[nrfields];
////		decimal        = new String[nrfields];
////		group          = new String[nrfields];
////		value          = new String[nrfields];
//	}
	
	public Object clone()
	{
		InfoExtractorMeta retval = (InfoExtractorMeta)super.clone();
		retval.resultField = resultField;
		retval.contentField = contentField;
		retval.extractType = extractType;
		retval.keyWord  = keyWord;
		retval.infoType = infoType;
		retval.regularExpression   = regularExpression;
		retval.isRemoveBlank = isRemoveBlank;
		retval.isRemoveControlChars = isRemoveControlChars;
		retval.isRemoveCRLF = isRemoveCRLF;
		retval.isRemoveHtml = isRemoveHtml;
//		retval.position = position;
//		retval.definedDigitals    = definedDigitals;
//		retval.order        = order;
//		retval.minLength     = minLength;
//		retval.maxLength     = maxLength;
//      retval.contentStartMark = contentStartMark;
//		retval.contentMode   = contentMode;
		return retval;
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{

		try
		{
			resultField = XMLHandler.getTagValue(stepnode, "result_field");
			contentField = XMLHandler.getTagValue(stepnode, "content_field");
			extractType = Const.toInt(XMLHandler.getTagValue(stepnode, "extract_type"),1);
			keyWord = XMLHandler.getTagValue(stepnode, "key_word");
			regularExpression = XMLHandler.getTagValue(stepnode, "regular_expression");
			infoType = Const.toInt(XMLHandler.getTagValue(stepnode, "info_type"),1);
			isRemoveHtml= "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "remove_html"));
			isRemoveBlank= "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "remove_blank"));
			isRemoveCRLF= "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "remove_crlf"));
			isRemoveControlChars= "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "remove_control_chars"));

			contentStartMark = XMLHandler.getTagValue(stepnode, "content_mark");
			position = XMLHandler.getTagValue(stepnode, "position");
			//definedDigitals    = XMLHandler.getTagValue(fnode, "definedDigitals");
			order    = Const.toInt(XMLHandler.getTagValue(stepnode, "order"), -1);
			minLength = Const.toInt(XMLHandler.getTagValue(stepnode, "minDigitals"), -1);
			maxLength = Const.toInt(XMLHandler.getTagValue(stepnode, "maxDigitals"), -1);
	    }
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void setDefault()
	{

	}
	
	public void getFields(RowMetaInterface rowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException
	{
		int type=ValueMetaInterface.TYPE_STRING;
		ValueMetaInterface v= null ;
		try {
		v = ValueMetaFactory.createValueMeta( resultField, type );
	} catch (KettlePluginException e) {
			v = new ValueMetaNone( resultField );
	}
		//ValueMetaInterface v=new ValueMeta(resultField, type);
		v.setOrigin(name);
		rowMeta.addValueMeta(v);
	}
		
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(300);

		retval.append("        ").append(XMLHandler.addTagValue("result_field", resultField));
		retval.append("        ").append(XMLHandler.addTagValue("content_field", contentField));
		retval.append("        ").append(XMLHandler.addTagValue("extract_type", extractType));
		retval.append("        ").append(XMLHandler.addTagValue("key_word", keyWord));
		retval.append("        ").append(XMLHandler.addTagValue("regular_expression", regularExpression));
		retval.append("        ").append(XMLHandler.addTagValue("info_type", infoType));
		retval.append("        ").append(XMLHandler.addTagValue("remove_html", isRemoveHtml));
		retval.append("        ").append(XMLHandler.addTagValue("remove_blank", isRemoveBlank));
		retval.append("        ").append(XMLHandler.addTagValue("remove_crlf", isRemoveCRLF));
		retval.append("        ").append(XMLHandler.addTagValue("remove_control_chars", isRemoveControlChars));

		retval.append("        ").append(XMLHandler.addTagValue("content_mark", contentStartMark));
		retval.append("        ").append(XMLHandler.addTagValue("position",    position));
		//retval.append("        ").append(XMLHandler.addTagValue("definedDigitals",  definedDigitals));
		retval.append("        ").append(XMLHandler.addTagValue("order",    order));
		retval.append("        ").append(XMLHandler.addTagValue("minDigitals", minLength));
		retval.append("        ").append(XMLHandler.addTagValue("maxDigitals", maxLength));

		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
			resultField =       rep.getStepAttributeString (id_step, "result_field");
			contentField =       rep.getStepAttributeString (id_step,  "content_field");
			extractType = (int) rep.getStepAttributeInteger (id_step,  "extract_type");
			keyWord     =       rep.getStepAttributeString (id_step,  "key_word");
			regularExpression     =       rep.getStepAttributeString (id_step,  "regular_xpression");
			infoType      = (int) rep.getStepAttributeInteger (id_step,  "info_type");
			isRemoveHtml = rep.getStepAttributeBoolean( id_step, "remove_html" );
			isRemoveBlank = rep.getStepAttributeBoolean( id_step, "remove_blank" );
			isRemoveCRLF = rep.getStepAttributeBoolean( id_step, "remove_crlf" );
			isRemoveControlChars = rep.getStepAttributeBoolean( id_step, "remove_control_chars" );


			contentStartMark =       rep.getStepAttributeString (id_step,  "content_mark");
			position    =       rep.getStepAttributeString (id_step,  "position");
			//definedDigitalStr       =       rep.getStepAttributeString (id_step,  "difine_digitals");
			order    =  (int)rep.getStepAttributeInteger(id_step,  "order");
			minLength =  (int)rep.getStepAttributeInteger(id_step, "min_length");
			maxLength =  (int)rep.getStepAttributeInteger(id_step, "max_length");

		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "result_field", resultField);
			rep.saveStepAttribute(id_transformation, id_step, "content_field", contentField);
			rep.saveStepAttribute(id_transformation, id_step, "extract_type",    extractType);
			rep.saveStepAttribute(id_transformation, id_step,  "key_word",      keyWord);
			rep.saveStepAttribute(id_transformation, id_step,  "regular_xpression",      regularExpression);
			rep.saveStepAttribute(id_transformation, id_step,  "info_type",    infoType);
			rep.saveStepAttribute( id_transformation, id_step, "remove_html", isRemoveHtml );
			rep.saveStepAttribute( id_transformation, id_step, "remove_blank", isRemoveBlank );
			rep.saveStepAttribute( id_transformation, id_step, "remove_crlf", isRemoveCRLF );
			rep.saveStepAttribute( id_transformation, id_step, "remove_control_chars", isRemoveControlChars );

			rep.saveStepAttribute(id_transformation, id_step, "content_mark", contentStartMark);
			rep.saveStepAttribute(id_transformation, id_step,  "position",    position);
			//rep.saveStepAttribute(id_transformation, id_step,  "define_digitals",  definedDigitals);
			rep.saveStepAttribute(id_transformation, id_step,  "order",    order);
			rep.saveStepAttribute(id_transformation, id_step,  "min_length", minLength);
			rep.saveStepAttribute(id_transformation, id_step,  "max_length", maxLength);

		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG,"ConstantMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,"ConstantMeta.CheckResult.NoFields"), stepMeta);
			remarks.add(cr);
		}
        

		InfoExtractorData data = new InfoExtractorData();
		InfoExtractorMeta meta = (InfoExtractorMeta) stepMeta.getStepMetaInterface();
        //Constant.buildRow(meta, data, remarks); //fixme, comment it  temprarily
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new InfoExtractor(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new InfoExtractorData();
	}

	public String getResultField() {
		return resultField;
	}

	public void setResultField(String resultField) {
		this.resultField = resultField;
	}

	public String getContentStartMark() {
		return contentStartMark;
	}

	public void setContentStartMark(String contentStartMark) {
		this.contentStartMark = contentStartMark;
	}

	public int getExtractType() {
		return extractType;
	}
	public void setExtractType(int extractType) {
		this.extractType = extractType;
	}

	public int getInfoType() {
		return infoType;
	}
	public void setInfoType(int t) {
		this.infoType = t;
	}

	public void setRemoveHtml(boolean removeHtml) {
		isRemoveHtml = removeHtml;
	}

	public void setRemoveBlank(boolean removeBlank) {
		isRemoveBlank = removeBlank;
	}

	public void setRemoveControlChars(boolean removeControlChars) {
		isRemoveControlChars = removeControlChars;
	}

	public void setRemoveCRLF(boolean removeCRLF) {
		isRemoveCRLF = removeCRLF;
	}

	public boolean isRemoveHtml() {
		return isRemoveHtml;
	}

	public boolean isRemoveBlank() {
		return isRemoveBlank;
	}

	public boolean isRemoveControlChars() {
		return isRemoveControlChars;
	}

	public boolean isRemoveCRLF() {
		return isRemoveCRLF;
	}
}
