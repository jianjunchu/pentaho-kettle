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

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
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
import org.w3c.dom.Node;


public class InfoExtractorMeta extends BaseStepMeta implements StepMetaInterface
{	
	private static Class<?> PKG = InfoExtractorMeta.class;
	private  String definedDigitals[];
//	private  String decimal[];
//	private  String group[];
//	private  String value[];
	
	private  String fieldName[];
	private  String contentMode[];
	private  String keyWord[];
	private  String position[];

	private  int order[];
	private  int minLength[];
	private  int maxLength[];
	private  String searchtMode[];
	
	private String resultField;
	private String contentStartMark;
	private String contentEndMark;


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
    public String[] getPosition()
    {
        return position;
    }
    
    /**
     * @param position The fieldFormat to set.
     */
    public void setPosition(String[] position)
    {
        this.position = position;
    }
    
    /**
     * @return Returns the fieldLength.
     */
    public int[] getOrder()
    {
        return order;
    }
    
    /**
     * @param order The fieldLength to set.
     */
    public void setOrder(int[] order)
    {
        this.order = order;
    }
    
    /**
     * @return Returns the fieldName.
     */
    public String[] getFieldName()
    {
        return fieldName;
    }
    
    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String[] fieldName)
    {
        this.fieldName = fieldName;
    }
    
    /**
     * @return Returns the fieldPrecision.
     */
    public int[] getMinLength()
    {
        return minLength;
    }
    
    /**
     * @return Returns the fieldPrecision.
     */
    public int[] getMaxLength()
    {
        return maxLength;
    }
    
    /**
     * @param digitals
     */
    public void setMinLength(int[] digitals)
    {
        this.minLength = digitals;
    }
    
    /**
     * @param digitals
     */
    public void setMaxLength(int[] digitals)
    {
        this.maxLength = digitals;
    }
    /**
     * @return Returns the fieldType.
     */
    public String[] getKeyWord()
    {
        return keyWord;
    }
    
    /**
     * @param keyWord The fieldType to set.
     */
    public void setKeyWord(String[] keyWord)
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

	public void allocate(int nrfields)
	{
		fieldName      = new String[nrfields];
		contentMode = new String[nrfields];
		keyWord      = new String[nrfields];
		position    = new String[nrfields];
		order    = new int[nrfields];
		minLength = new int[nrfields];
		maxLength = new int[nrfields];
		definedDigitals       = new String[nrfields];
//		decimal        = new String[nrfields];
//		group          = new String[nrfields];
//		value          = new String[nrfields];
	}
	
	public Object clone()
	{
		InfoExtractorMeta retval = (InfoExtractorMeta)super.clone();
		
		retval.resultField = resultField;
		retval.contentStartMark = contentStartMark;
		
		int nrfields=fieldName.length;

		retval.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.fieldName[i]   = fieldName[i];
			retval.contentMode[i]   = contentMode[i];
			retval.keyWord[i]   = keyWord[i];
			retval.position[i] = position[i];
			retval.definedDigitals[i]    = definedDigitals[i];
			retval.order[i]        = order[i]; 
			retval.minLength[i]     = minLength[i];
			retval.maxLength[i]     = maxLength[i]; 
		}
		
		return retval;
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			this.resultField = XMLHandler.getTagValue(stepnode, "content_field");
			this.contentStartMark = XMLHandler.getTagValue(stepnode, "content_mark");
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int  nrfields=XMLHandler.countNodes(fields, "field");
	
			allocate(nrfields);
			
			String sorder, sminDigitals, smaxDigitals;
			
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				
				fieldName[i]   = XMLHandler.getTagValue(fnode, "name");
				contentMode[i]   = XMLHandler.getTagValue(fnode, "contentMode");
				keyWord[i]   = XMLHandler.getTagValue(fnode, "keyWord");
				position[i] = XMLHandler.getTagValue(fnode, "position");
				definedDigitals[i]    = XMLHandler.getTagValue(fnode, "definedDigitals");
				sorder        = XMLHandler.getTagValue(fnode, "order");
				sminDigitals     = XMLHandler.getTagValue(fnode, "minDigitals");
				smaxDigitals     = XMLHandler.getTagValue(fnode, "maxDigitals");
				order[i]    = Const.toInt(sorder, -1);
				minLength[i] = Const.toInt(sminDigitals, -1);
				maxLength[i] = Const.toInt(smaxDigitals, -1);
			}
        }
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void setDefault()
	{
		int i, nrfields=0;
	
		allocate(nrfields);

        DecimalFormat decimalFormat = new DecimalFormat();

		for (i=0;i<nrfields;i++)
		{
			fieldName[i]      = "field"+i;				
			keyWord[i]      = "Number";
			position[i]    = "\u00A40,000,000.00;\u00A4-0,000,000.00";
			order[i]    = 9;
			minLength[i] = 2;
			maxLength[i] = 10;
			definedDigitals[i]       = decimalFormat.getDecimalFormatSymbols().getCurrencySymbol();
//			decimal[i]        = new String(new char[] { decimalFormat.getDecimalFormatSymbols().getDecimalSeparator() } );
//			group[i]          = new String(new char[] { decimalFormat.getDecimalFormatSymbols().getGroupingSeparator() } );
//			value[i]          = "-";
		}

	}
	
	public void getFields(RowMetaInterface rowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		for (int i=0;i<fieldName.length;i++)
		{
			if (fieldName[i]!=null && fieldName[i].length()!=0)
			{
				int type=ValueMeta.getType(keyWord[i]);
				if (type==ValueMetaInterface.TYPE_NONE) type=ValueMetaInterface.TYPE_STRING;
				ValueMetaInterface v=new ValueMeta(fieldName[i], type);
				v.setLength(order[i]);
                v.setPrecision(minLength[i]);
				v.setOrigin(name);
				rowMeta.addValueMeta(v);
			}
		}
	}
		
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(300);
		
		
		retval.append("        ").append(XMLHandler.addTagValue("content_field", resultField));
		retval.append("        ").append(XMLHandler.addTagValue("content_mark", contentStartMark));
		
		retval.append("    <fields>").append(Const.CR);
		for (int i=0;i<fieldName.length;i++)
		{
			if (fieldName[i]!=null && fieldName[i].length()!=0)
			{
				retval.append("      <field>").append(Const.CR);
				retval.append("        ").append(XMLHandler.addTagValue("name",      fieldName[i]));
				retval.append("        ").append(XMLHandler.addTagValue("contentMode", contentMode[i]));
				retval.append("        ").append(XMLHandler.addTagValue("keyWord",      keyWord[i]));
				retval.append("        ").append(XMLHandler.addTagValue("position",    position[i]));
				retval.append("        ").append(XMLHandler.addTagValue("definedDigitals",  definedDigitals[i]));
				retval.append("        ").append(XMLHandler.addTagValue("order",    order[i]));
				retval.append("        ").append(XMLHandler.addTagValue("minDigitals", minLength[i]));
				retval.append("        ").append(XMLHandler.addTagValue("maxDigitals", maxLength[i]));
				retval.append("      </field>").append(Const.CR);
			}
		}
		retval.append("    </fields>").append(Const.CR);

		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");

			resultField =       rep.getStepAttributeString (id_step, "content_field");
			contentStartMark =       rep.getStepAttributeString (id_step,  "content_mark");

			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				fieldName[i]      =       rep.getStepAttributeString (id_step, i, "field_name");
				contentMode[i]      =       rep.getStepAttributeString (id_step, i, "content_mode");
				keyWord[i]      =       rep.getStepAttributeString (id_step, i, "key_word");
	
				position[i]    =       rep.getStepAttributeString (id_step, i, "position");
				definedDigitals[i]       =       rep.getStepAttributeString (id_step, i, "difine_digitals");
				order[i]    =  (int)rep.getStepAttributeInteger(id_step, i, "order");
				minLength[i] =  (int)rep.getStepAttributeInteger(id_step, i, "min_length");
				maxLength[i] =  (int)rep.getStepAttributeInteger(id_step, i, "max_length");
			}
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
			rep.saveStepAttribute(id_transformation, id_step, "content_field", resultField);
			rep.saveStepAttribute(id_transformation, id_step, "content_mark", contentStartMark);

			for (int i=0;i<fieldName.length;i++)
			{
				if (fieldName[i]!=null && fieldName[i].length()!=0)
				{
					rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      fieldName[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "content_mode",    contentMode[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "key_word",      keyWord[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "position",    position[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "define_digitals",  definedDigitals[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "order",    order[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "min_length", minLength[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "max_length", maxLength[i]);
				}
			}
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

	public String[] getContentMode() {
		return contentMode;
	}

	public void setContentMode(String[] contentMode) {
		this.contentMode = contentMode;
	}

	public String[] getSearchtMode() {
		return searchtMode;
	}

	public void setSearchtMode(String[] searchtMode) {
		this.searchtMode = searchtMode;
	}
	
}
