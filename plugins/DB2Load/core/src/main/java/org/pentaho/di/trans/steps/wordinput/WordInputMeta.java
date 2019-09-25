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

package org.pentaho.di.trans.steps.wordinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.KettleAttributeInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.w3c.dom.Node;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.row.value.ValueMetaString;

/**
 * @since 2007-07-05
 * @author matt
 * @version 3.0
 */
@Step( id = "WORDINPUT", image = "WDI.png", i18nPackageName = "org.pentaho.di.trans.steps.wordinput",
		name = "WordInput.Step.Name", description = "WordInput.Step.Desc", categoryDescription = "Input",
		documentationUrl = "Products/Data_Integration/Transformation_Step_Reference/Word_Input" )
@InjectionSupported( localizationPrefix = "WordInput.Injection.", groups = { "INPUT_FIELDS" } )
public class WordInputMeta extends BaseStepMeta implements StepMetaInterface, StepMetaInjectionInterface
{
	private static final String JSON_FIELD_NAME = "content";
	private static Class<?> PKG = org.pentaho.di.trans.steps.csvinput.CsvInput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private String filename;
	
	private String filenameField;

	private boolean includingFilename; 
	
	private String rowNumField;

	private boolean headerPresent;

	private String startRowIndex;

	private String tableNr;

	private TextFileInputField[] inputFields;
	
	private boolean isaddresult;
	private int nrHeaderLines =1; //head rows, default 1 row

	public boolean isExtractSpecifiedTable() {
		return extractSpecifiedTable;
	}

	public void setExtractSpecifiedTable(boolean extractSpecifiedTable) {
		this.extractSpecifiedTable = extractSpecifiedTable;
	}

	private boolean extractSpecifiedTable = false;
	public WordInputMeta()
	{
		super(); // allocate BaseStepMeta
		allocate(0);
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}

	public void setDefault() {
		startRowIndex = "0"  ;
		headerPresent = true;
		isaddresult=false;
		tableNr="0";
	}

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			filename = XMLHandler.getTagValue(stepnode, getXmlCode("FILENAME"));
			filenameField = XMLHandler.getTagValue(stepnode, getXmlCode("FILENAME_FIELD"));
			rowNumField = XMLHandler.getTagValue(stepnode, getXmlCode("ROW_NUM_FIELD"));
			includingFilename = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, getXmlCode("INCLUDE_FILENAME")));
			startRowIndex = XMLHandler.getTagValue(stepnode, getXmlCode("START_ROW_INDEX"));
			tableNr  = XMLHandler.getTagValue(stepnode, getXmlCode("TABLE_NR"));
			headerPresent = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, getXmlCode("HEADER_PRESENT")));
			isaddresult= "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, getXmlCode("ADD_FILENAME_RESULT")));

			Node fields = XMLHandler.getSubNode(stepnode, getXmlCode("FIELDS"));
			int nrfields = XMLHandler.countNodes(fields, getXmlCode("FIELD"));
			
			allocate(nrfields);

			for (int i = 0; i < nrfields; i++)
			{
				inputFields[i] = new TextFileInputField();
				
				Node fnode = XMLHandler.getSubNodeByNr(fields, getXmlCode("FIELD"), i);

				inputFields[i].setName( XMLHandler.getTagValue(fnode, getXmlCode("FIELD_NAME")) );
				inputFields[i].setType(  ValueMeta.getType(XMLHandler.getTagValue(fnode, getXmlCode("FIELD_TYPE"))) );
				inputFields[i].setFormat( XMLHandler.getTagValue(fnode, getXmlCode("FIELD_FORMAT")) );
				inputFields[i].setCurrencySymbol( XMLHandler.getTagValue(fnode, getXmlCode("FIELD_CURRENCY")) );
				inputFields[i].setDecimalSymbol( XMLHandler.getTagValue(fnode, getXmlCode("FIELD_DECIMAL")) );
				inputFields[i].setGroupSymbol( XMLHandler.getTagValue(fnode, getXmlCode("FIELD_GROUP")) );
				inputFields[i].setLength( Const.toInt(XMLHandler.getTagValue(fnode, getXmlCode("FIELD_LENGTH")), -1) );
				inputFields[i].setPrecision( Const.toInt(XMLHandler.getTagValue(fnode, getXmlCode("FIELD_PRECISION")), -1) );
				inputFields[i].setTrimType( ValueMeta.getTrimTypeByCode( XMLHandler.getTagValue(fnode, getXmlCode("FIELD_TRIM_TYPE")) ) );
			}
		}
		catch (Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void allocate(int nrFields) {
		inputFields = new TextFileInputField[nrFields];
	}

	public String getXML()
	{
		StringBuffer retval = new StringBuffer(500);
		
		retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("FILENAME"), filename));
		retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("FILENAME_FIELD"), filenameField));
		retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("ROW_NUM_FIELD"), rowNumField));
		retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("INCLUDE_FILENAME"), includingFilename));
		retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("START_ROW_INDEX"), startRowIndex));
		retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("HEADER_PRESENT"), headerPresent));
		retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("TABLE_NR"), tableNr));
		retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("ADD_FILENAME_RESULT"), isaddresult));

		retval.append("    ").append(XMLHandler.openTag(getXmlCode("FIELDS"))).append(Const.CR);
		for (int i = 0; i < inputFields.length; i++)
		{
			TextFileInputField field = inputFields[i];
			
	        retval.append("      ").append(XMLHandler.openTag(getXmlCode("FIELD"))).append(Const.CR);
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("FIELD_NAME"), field.getName()));
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("FIELD_TYPE"), ValueMeta.getTypeDesc(field.getType())));
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("FIELD_FORMAT"), field.getFormat()));
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("FIELD_CURRENCY"), field.getCurrencySymbol()));
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("FIELD_DECIMAL"), field.getDecimalSymbol()));
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("FIELD_GROUP"), field.getGroupSymbol()));
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("FIELD_LENGTH"), field.getLength()));
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("FIELD_PRECISION"), field.getPrecision()));
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("FIELD_TRIM_TYPE"), ValueMeta.getTrimTypeCode(field.getTrimType())));
            retval.append("      ").append(XMLHandler.closeTag(getXmlCode("FIELD"))).append(Const.CR);
		}
        retval.append("    ").append(XMLHandler.closeTag(getXmlCode("FIELDS"))).append(Const.CR);

		return retval.toString();
	}


	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			filename = rep.getStepAttributeString(id_step, getRepCode("FILENAME"));
			filenameField = rep.getStepAttributeString(id_step, getRepCode("FILENAME_FIELD"));
			rowNumField = rep.getStepAttributeString(id_step, getRepCode("ROW_NUM_FIELD"));
			includingFilename = rep.getStepAttributeBoolean(id_step, getRepCode("INCLUDE_FILENAME"));
			startRowIndex = rep.getStepAttributeString(id_step, getRepCode("START_ROW_INDEX"));
			headerPresent = rep.getStepAttributeBoolean(id_step, getRepCode("HEADER_PRESENT"));
			tableNr = rep.getStepAttributeString(id_step, getRepCode("TABLE_NR"));
			isaddresult = rep.getStepAttributeBoolean(id_step, getRepCode("ADD_FILENAME_RESULT"));

			int nrfields = rep.countNrStepAttributes(id_step, getRepCode("FIELD_NAME"));

			allocate(nrfields);

			for (int i = 0; i < nrfields; i++)
			{
				inputFields[i] = new TextFileInputField();
				
				inputFields[i].setName( rep.getStepAttributeString(id_step, i, getRepCode("FIELD_NAME")) );
				inputFields[i].setType( ValueMeta.getType(rep.getStepAttributeString(id_step, i, getRepCode("FIELD_TYPE"))) );
				inputFields[i].setFormat( rep.getStepAttributeString(id_step, i, getRepCode("FIELD_FORMAT")) );
				inputFields[i].setCurrencySymbol( rep.getStepAttributeString(id_step, i, getRepCode("FIELD_CURRENCY")) );
				inputFields[i].setDecimalSymbol( rep.getStepAttributeString(id_step, i, getRepCode("FIELD_DECIMAL")) );
				inputFields[i].setGroupSymbol( rep.getStepAttributeString(id_step, i, getRepCode("FIELD_GROUP")) );
				inputFields[i].setLength( (int) rep.getStepAttributeInteger(id_step, i, getRepCode("FIELD_LENGTH")) );
				inputFields[i].setPrecision( (int) rep.getStepAttributeInteger(id_step, i, getRepCode("FIELD_PRECISION")) );
				inputFields[i].setTrimType( ValueMeta.getTrimTypeByCode( rep.getStepAttributeString(id_step, i, getRepCode("FIELD_TRIM_TYPE"))) );
			}
		}
		catch (Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, getRepCode("FILENAME"), filename);
			rep.saveStepAttribute(id_transformation, id_step, getRepCode("FILENAME_FIELD"), filenameField);
			rep.saveStepAttribute(id_transformation, id_step, getRepCode("ROW_NUM_FIELD"), rowNumField);
			rep.saveStepAttribute(id_transformation, id_step, getRepCode("INCLUDE_FILENAME"), includingFilename);
			rep.saveStepAttribute(id_transformation, id_step, getRepCode("START_ROW_INDEX"), startRowIndex);
			rep.saveStepAttribute(id_transformation, id_step, getRepCode("TABLE_NR"), tableNr);
			rep.saveStepAttribute(id_transformation, id_step, getRepCode("HEADER_PRESENT"), headerPresent);
			rep.saveStepAttribute(id_transformation, id_step, getRepCode("ADD_FILENAME_RESULT"), isaddresult);

			for (int i = 0; i < inputFields.length; i++)
			{
				TextFileInputField field = inputFields[i];
				
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("FIELD_NAME"), field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("FIELD_TYPE"), ValueMeta.getTypeDesc(field.getType()));
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("FIELD_FORMAT"), field.getFormat());
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("FIELD_CURRENCY"), field.getCurrencySymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("FIELD_DECIMAL"), field.getDecimalSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("FIELD_GROUP"), field.getGroupSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("FIELD_LENGTH"), field.getLength());
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("FIELD_PRECISION"), field.getPrecision());
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("FIELD_TRIM_TYPE"), ValueMeta.getTrimTypeCode( field.getTrimType()));
			}
		}
		catch (Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
		}
	}
	
	public void getTableFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		rowMeta.clear(); // Start with a clean slate, eats the input
		
		for (int i=0;i<inputFields.length;i++) {
			TextFileInputField field = inputFields[i];
			
			ValueMetaInterface valueMeta = new ValueMeta(field.getName(), field.getType());
			valueMeta.setConversionMask( field.getFormat() );
			valueMeta.setLength( field.getLength() );
			valueMeta.setPrecision( field.getPrecision() );
			valueMeta.setConversionMask( field.getFormat() );
			valueMeta.setDecimalSymbol( field.getDecimalSymbol() );
			valueMeta.setGroupingSymbol( field.getGroupSymbol() );
			valueMeta.setCurrencySymbol( field.getCurrencySymbol() );
			valueMeta.setTrimType( field.getTrimType() );
			// In case we want to convert Strings...
			// Using a copy of the valueMeta object means that the inner and outer representation format is the same.
			// Preview will show the data the same way as we read it.
			// This layout is then taken further down the road by the metadata through the transformation.
			//
			ValueMetaInterface storageMetadata = valueMeta.clone();
			storageMetadata.setType(ValueMetaInterface.TYPE_STRING);
			storageMetadata.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
			storageMetadata.setLength(-1,-1); // we don't really know the lengths of the strings read in advance.
			valueMeta.setStorageMetadata(storageMetadata);
			
			valueMeta.setOrigin(origin);
			
			rowMeta.addValueMeta(valueMeta);
		}
		
		if (!Const.isEmpty(filenameField) && includingFilename) {
			ValueMetaInterface filenameMeta = new ValueMeta(filenameField, ValueMetaInterface.TYPE_STRING);
			filenameMeta.setOrigin(origin);
			rowMeta.addValueMeta(filenameMeta);
		}
		
		if (!Const.isEmpty(rowNumField)) {
			ValueMetaInterface rowNumMeta = new ValueMeta(rowNumField, ValueMetaInterface.TYPE_INTEGER);
			rowNumMeta.setLength(10);
			rowNumMeta.setOrigin(origin);
			rowMeta.addValueMeta(rowNumMeta);
		}
		
	}
	public void getFixedFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		rowMeta.clear(); // Start with a clean slate, eats the input
//
//		if (!Const.isEmpty(filenameField) && includingFilename) {
//			ValueMetaInterface filenameMeta = new ValueMeta(filenameField, ValueMetaInterface.TYPE_STRING);
//			filenameMeta.setOrigin(origin);
//			rowMeta.addValueMeta(filenameMeta);
//		}
//
//		if (!Const.isEmpty(rowNumField)) {
//			ValueMetaInterface rowNumMeta = new ValueMeta(rowNumField, ValueMetaInterface.TYPE_INTEGER);
//			rowNumMeta.setLength(10);
//			rowNumMeta.setOrigin(origin);
//			rowMeta.addValueMeta(rowNumMeta);
//		}

		ValueMetaInterface jsonContentMeta = new ValueMeta(JSON_FIELD_NAME, ValueMetaInterface.TYPE_STRING);
		jsonContentMeta.setOrigin(origin);
		rowMeta.addValueMeta(jsonContentMeta);

	}
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "CsvInputMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "CsvInputMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "CsvInputMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "CsvInputMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	@Override
	public void getFields( RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep,
						   VariableSpace space, Repository repository, IMetaStore metaStore ) {

		// clear the output
		r.clear();
		// append the outputFields to the output
		if(this.isExtractSpecifiedTable()) {
			for (int i = 0; i < inputFields.length; i++) {
				ValueMetaInterface v;
				try {
					v = ValueMetaFactory.createValueMeta(inputFields[i].getName(), inputFields[i].getType());
				} catch (KettlePluginException e) {
					v = new ValueMetaString(inputFields[i].getName());
				}
				// that would influence the output
				// v.setConversionMask(conversionMask[i]);
				v.setOrigin(origin);
				r.addValueMeta(v);
			}
		}else
		{
			ValueMetaInterface v;
			try {
				v = ValueMetaFactory.createValueMeta(JSON_FIELD_NAME, ValueMetaInterface.TYPE_STRING);
			} catch (KettlePluginException e) {
				v = new ValueMetaString(JSON_FIELD_NAME);
			}
			// that would influence the output
			// v.setConversionMask(conversionMask[i]);
			v.setOrigin(origin);
			r.addValueMeta(v);
		}

	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new WordInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new WordInputData();
	}

	/**
	 * @return the delimiter
	 */
	public String getStartRowIndex() {
		return startRowIndex;
	}

	/**
	 * @param index the index of start row
	 */
	public void setStartRowIndex(String index) {
		this.startRowIndex = index;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the table nr
	 */
	public String getTableNr() {
		return tableNr;
	}

	/**
	 * @param tableNr the table nr to set
	 */
	public void setTableNr(String tableNr) {
		this.tableNr = tableNr;
	}


	/**
	 * @return the headerPresent
	 */
	public boolean isHeaderPresent() {
		return headerPresent;
	}

	/**
	 * @param headerPresent the headerPresent to set
	 */
	public void setHeaderPresent(boolean headerPresent) {
		this.headerPresent = headerPresent;
	}

    @Override
	public List<ResourceReference> getResourceDependencies(TransMeta transMeta, StepMeta stepInfo) {
		List<ResourceReference> references = new ArrayList<ResourceReference>(5);

		ResourceReference reference = new ResourceReference(stepInfo);
		references.add(reference);
		if (!Const.isEmpty(filename)) {
			// Add the filename to the references, including a reference to this
			// step meta data.
			//
			reference.getEntries().add(new ResourceEntry(transMeta.environmentSubstitute(filename), ResourceType.FILE));
		}
		return references;
	}

	/**
	 * @return the inputFields
	 */
	public TextFileInputField[] getInputFields() {
		return inputFields;
	}

	/**
	 * @param inputFields
	 *            the inputFields to set
	 */
	public void setInputFields(TextFileInputField[] inputFields) {
		this.inputFields = inputFields;
	}

	public int getFileFormatTypeNr() {
		return TextFileInputMeta.FILE_FORMAT_MIXED; // TODO: check this
	}

	public String[] getFilePaths(VariableSpace space) {
		return new String[] { space.environmentSubstitute(filename), };
	}

	public int getNrHeaderLines() {
		return nrHeaderLines;
	}

	public boolean hasHeader() {
		return isHeaderPresent();
	}

	public String getErrorCountField() {
		return null;
	}

	public String getErrorFieldsField() {
		return null;
	}

	public String getErrorTextField() {
		return null;
	}

	public String getEscapeCharacter() {
		return null;
	}

	public String getFileType() {
		return "CSV";
	}

//	public String getSeparator() {
//		return startRowIndex;
//	}

	public boolean includeFilename() {
		return false;
	}

	public boolean includeRowNumber() {
		return false;
	}

	public boolean isErrorIgnored() {
		return false;
	}

	public boolean isErrorLineSkipped() {
		return false;
	}

	/**
	 * @return the filenameField
	 */
	public String getFilenameField() {
		return filenameField;
	}

	/**
	 * @param filenameField the filenameField to set
	 */
	public void setFilenameField(String filenameField) {
		this.filenameField = filenameField;
	}

	/**
	 * @return the includingFilename
	 */
	public boolean isIncludingFilename() {
		return includingFilename;
	}

	/**
	 * @param includingFilename the includingFilename to set
	 */
	public void setIncludingFilename(boolean includingFilename) {
		this.includingFilename = includingFilename;
	}

	/**
	 * @return the rowNumField
	 */
	public String getRowNumField() {
		return rowNumField;
	}

	/**
	 * @param rowNumField the rowNumField to set
	 */
	public void setRowNumField(String rowNumField) {
		this.rowNumField = rowNumField;
	}	
	
	 /**
     * @param isaddresult The isaddresult to set.
     */
    public void setAddResultFile(boolean isaddresult)
    {
        this.isaddresult = isaddresult;
    }
    
    /**
     *  @return Returns isaddresult.
     */
    public boolean isAddResultFile()
    {
        return isaddresult;
    }

	/**
	 * Since the exported transformation that runs this will reside in a ZIP file, we can't reference files relatively.
	 * So what this does is turn the name of files into absolute paths OR it simply includes the resource in the ZIP file.
	 * For now, we'll simply turn it into an absolute path and pray that the file is on a shared drive or something like that.

	 * TODO: create options to configure this behavior 
	 */
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface, Repository repository) throws KettleException {
		try {
			// The object that we're modifying here is a copy of the original!
			// So let's change the filename from relative to absolute by grabbing the file object...
			// In case the name of the file comes from previous steps, forget about this!
			//
			if (Const.isEmpty(filenameField)) {
				// From : ${Internal.Transformation.Filename.Directory}/../foo/bar.csv
				// To   : /home/matt/test/files/foo/bar.csv
				//
				FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(filename), space);
				
				// If the file doesn't exist, forget about this effort too!
				//
				if (fileObject.exists()) {
					// Convert to an absolute path...
					// 
					filename = resourceNamingInterface.nameResource(fileObject, space, true);
					
					return filename;
				}
			}
			return null;
		} catch (Exception e) {
			throw new KettleException(e); //$NON-NLS-1$
		}
	}
	
	public boolean supportsErrorHandling() {
		return true;
	}
	
	public StepMetaInjectionInterface getStepMetaInjectionInterface() {
	  
	  return this;
	}

    public void injectStepMetadataEntries(List<StepInjectionMetaEntry> metadata) {
      for (StepInjectionMetaEntry entry : metadata) {
        KettleAttributeInterface attr = findAttribute(entry.getKey());
        
        // Set top level attributes...
        //
        if (entry.getValueType()!=ValueMetaInterface.TYPE_NONE) {
          if (attr.getKey().equals("FILENAME")) { filename = (String) entry.getValue(); } else
          if (attr.getKey().equals("FILENAME_FIELD")) { filenameField = (String) entry.getValue(); } else
          if (attr.getKey().equals("ROW_NUM_FIELD")) { rowNumField = (String) entry.getValue(); } else
          if (attr.getKey().equals("HEADER_PRESENT")) { headerPresent = (Boolean) entry.getValue(); } else
          if (attr.getKey().equals("START_ROW_INDEX")) { startRowIndex = (String) entry.getValue();} else
          if (attr.getKey().equals("TABLE_NR")) { tableNr = (String) entry.getValue(); } else
          if (attr.getKey().equals("ADD_FILENAME_RESULT")) { isaddresult = (Boolean) entry.getValue(); }  else
          { 
            throw new RuntimeException("Unhandled metadata injection of attribute: "+attr.toString()+" - "+attr.getDescription());
          }
        } else {
          if (attr.getKey().equals("FIELDS")) {
            // This entry contains a list of lists...
            // Each list contains a single CSV input field definition (one line in the dialog)
            //
            List<StepInjectionMetaEntry> inputFieldEntries = entry.getDetails();
            inputFields = new TextFileInputField[inputFieldEntries.size()];
            for (int row=0;row<inputFieldEntries.size();row++) {
              StepInjectionMetaEntry inputFieldEntry = inputFieldEntries.get(row);
              TextFileInputField inputField = new TextFileInputField();

              List<StepInjectionMetaEntry> fieldAttributes = inputFieldEntry.getDetails();
              for (int i=0;i<fieldAttributes.size();i++) {
                StepInjectionMetaEntry fieldAttribute = fieldAttributes.get(i);
                KettleAttributeInterface fieldAttr = findAttribute(fieldAttribute.getKey());

                String attributeValue = (String)fieldAttribute.getValue();
                if (fieldAttr.getKey().equals("FIELD_NAME")) { inputField.setName(attributeValue); } else 
                if (fieldAttr.getKey().equals("FIELD_TYPE")) { inputField.setType(ValueMeta.getType(attributeValue)); } else 
                if (fieldAttr.getKey().equals("FIELD_FORMAT")) { inputField.setFormat(attributeValue); } else 
                if (fieldAttr.getKey().equals("FIELD_LENGTH")) { inputField.setLength(attributeValue==null ? -1 : Integer.parseInt(attributeValue)); } else 
                if (fieldAttr.getKey().equals("FIELD_PRECISION")) { inputField.setPrecision(attributeValue==null ? -1 : Integer.parseInt(attributeValue)); } else 
                if (fieldAttr.getKey().equals("FIELD_CURRENCY")) { inputField.setCurrencySymbol(attributeValue); } else 
                if (fieldAttr.getKey().equals("FIELD_DECIMAL")) { inputField.setDecimalSymbol(attributeValue); } else 
                if (fieldAttr.getKey().equals("FIELD_GROUP")) { inputField.setGroupSymbol(attributeValue); } else 
                if (fieldAttr.getKey().equals("FIELD_TRIM_TYPE")) { inputField.setTrimType(ValueMeta.getTrimTypeByCode(attributeValue)); } else
                {
                  throw new RuntimeException("Unhandled metadata injection of attribute: "+fieldAttr.toString()+" - "+fieldAttr.getDescription());
                }
              }
              
              inputFields[row] = inputField;
            }
          }
        }
      }
    }

    /**
     * Describe the metadata attributes that can be injected into this step metadata object.
     * @throws KettleException 
     */
    public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
      return getStepInjectionMetadataEntries(PKG);
    }

	@Override
	public String getDialogClassName() {
		return WordInputDialog.class.getName();
	}

	public boolean extractTable() {
		return extractSpecifiedTable;
	}

}