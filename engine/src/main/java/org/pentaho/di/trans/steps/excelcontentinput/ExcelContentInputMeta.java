/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.excelcontentinput;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.*;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.fileinput.NonAccessibleFileObject;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.excelinput.SpreadSheetType;
import org.pentaho.di.trans.steps.excelinput.WorkbookFactory;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

public class ExcelContentInputMeta extends BaseStepMeta implements StepMetaInterface {
  private static final String INCLUDE = "include";
  private static final String INCLUDE_FIELD = "include_field";
  private static final String ROWNUM = "rownum";
  private static final String ADDRESULTFILE = "addresultfile";
  private static final String IS_IGNORE_EMPTY_FILE = "IsIgnoreEmptyFile";
  private static final String IS_IGNORE_MISSING_PATH = "IsIgnoreMissingPath";
  private static final String ROWNUM_FIELD = "rownum_field";
  private static final String ENCODING = "encoding";
  private static final String NAME = "name";
  private static final String EXCLUDE_FILEMASK = "exclude_filemask";
  private static final String FILE_REQUIRED = "file_required";
  private static final String INCLUDE_SUBFOLDERS = "include_subfolders";
  private static final String TEMPLATE_FILE = "template_file";
//  private static final String IS_IN_FIELDS = "IsInFields";
//  private static final String DYNAMIC_FILENAME_FIELD = "DynamicFilenameField";
//  private static final String SHORT_FILE_FIELD_NAME = "shortFileFieldName";
//  private static final String PATH_FIELD_NAME = "pathFieldName";
//  private static final String HIDDEN_FIELD_NAME = "hiddenFieldName";
//  private static final String LAST_MODIFICATION_TIME_FIELD_NAME = "lastModificationTimeFieldName";
//  private static final String URI_NAME_FIELD_NAME = "uriNameFieldName";
//  private static final String ROOT_URI_NAME_FIELD_NAME = "rootUriNameFieldName";
//  private static final String EXTENSION_FIELD_NAME = "extensionFieldName";
  private static final String FILE = "file";
  private static final String FIELDS = "fields";

  // Repository constant not sync with xml just to backward compatibility
  private static final String FILE_NAME_REP = "file_name";
  private static final String FILE_MASK_REP = "file_mask";
  private static final String FILE_TYPE = "file_type";
  private static final String FILE_NAME_PART = "file_name_part";
  private static final String EXCLUDEFILE_MASK_REP = "excludefile_mask";
  private static final String FIELD_NAME_REP = "field_name";
//  private static final String ELEMENT_TYPE_REP = "element_type";
//  private static final String FIELD_TYPE_REP = "field_type";
//  private static final String FIELD_FORMAT_REP = "field_format";
//  private static final String FIELD_CURRENCY_REP = "field_currency";
//  private static final String FIELD_DECIMAL_REP = "field_decimal";
//  private static final String FIELD_GROUP_REP = "field_group";
//  private static final String FIELD_LENGTH_REP = "field_length";
//  private static final String FIELD_PRECISION_REP = "field_precision";
//  private static final String FIELD_TRIM_TYPE_REP = "field_trim_type";
//  private static final String FIELD_REPEAT_REP = "field_repeat";
  private static Class<?> PKG = ExcelContentInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String[] RequiredFilesDesc = new String[] { BaseMessages.getString( PKG, "System.Combo.No" ),
    BaseMessages.getString( PKG, "System.Combo.Yes" ) };
  public static final String[] RequiredFilesCode = new String[] { "N", "Y" };

  private static final String NO = "N";
  private static final String YES = "Y";

  /** filename or directory name */
  private String fileName;

  /** Wildcard or filemask (regular expression) generated by file type and filenamePart at runtime */
  private String fileMask;


  /** file types seperated by ; */
  private SpreadSheetType fileType= SpreadSheetType.POI;

  /** part of filename to filter */
  private String filenamePart;
  private String excludeFileMask;

  /** Flag indicating that we should include the filename in the output */
  //private boolean includeFilename;

  /** The name of the field in the output containing the filename */
  private String filenameField;

  /** Flag indicating that a row number field should be included in the output */
  //private boolean includeRowNumber;

  /** The name of the field in the output containing the row number */
  //private String rowNumberField;

  /** The maximum number or lines to read */
  private String templateFile;

  /** The fields to import... */
  private ExcelContentInputField[] inputFields = new ExcelContentInputField[4];

  /** The encoding to use for reading: null or empty string means system default encoding */
  private String encoding;

  /** Dynamic FilenameField */
  //private String DynamicFilenameField;

  /** Is In fields */
  private boolean fileinfield;

  /** Flag: add result filename **/
  private boolean addresultfile;

  /** Array of boolean values as string, indicating if a file is required. */
  private String[] fileRequired;

  /** Flag : do we ignore empty file? */
  private boolean IsIgnoreEmptyFile;

  /** Flag : do we ignore missing path? */
  private boolean IsIgnoreMissingPath;

  /** Array of boolean values as string, indicating if we need to fetch sub folders. */
  private boolean includeSubFolders;

  /** Additional fields **/
  private String shortFileFieldName;
  private String pathFieldName;
  private String hiddenFieldName;
  private String lastModificationTimeFieldName;
  private String uriNameFieldName;
  private String rootUriNameFieldName;
  private String extensionFieldName;

  public ExcelContentInputMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the shortFileFieldName.
   */
  public String getShortFileNameField() {
    return shortFileFieldName;
  }

  /**
   * @param field
   *          The shortFileFieldName to set.
   */
  public void setShortFileNameField( String field ) {
    shortFileFieldName = field;
  }

  /**
   * @return Returns the pathFieldName.
   */
  public String getPathField() {
    return pathFieldName;
  }

  /**
   * @param field
   *          The pathFieldName to set.
   */
  public void setPathField( String field ) {
    this.pathFieldName = field;
  }

  /**
   * @return Returns the hiddenFieldName.
   */
  public String isHiddenField() {
    return hiddenFieldName;
  }

  /**
   * @param field
   *          The hiddenFieldName to set.
   */
  public void setIsHiddenField( String field ) {
    hiddenFieldName = field;
  }

  /**
   * @return Returns the lastModificationTimeFieldName.
   */
  public String getLastModificationDateField() {
    return lastModificationTimeFieldName;
  }

  /**
   * @param field
   *          The lastModificationTimeFieldName to set.
   */
  public void setLastModificationDateField( String field ) {
    lastModificationTimeFieldName = field;
  }

  /**
   * @return Returns the uriNameFieldName.
   */
  public String getUriField() {
    return uriNameFieldName;
  }

  /**
   * @param field
   *          The uriNameFieldName to set.
   */
  public void setUriField( String field ) {
    uriNameFieldName = field;
  }

  /**
   * @return Returns the uriNameFieldName.
   */
  public String getRootUriField() {
    return rootUriNameFieldName;
  }

  /**
   * @param field
   *          The rootUriNameFieldName to set.
   */
  public void setRootUriField( String field ) {
    rootUriNameFieldName = field;
  }

  /**
   * @return Returns the extensionFieldName.
   */
  public String getExtensionField() {
    return extensionFieldName;
  }

  /**
   * @param field
   *          The extensionFieldName to set.
   */
  public void setExtensionField( String field ) {
    extensionFieldName = field;
  }

  public String[] getFileRequired() {
    return fileRequired;
  }

  public void setFileRequired( String[] fileRequired ) {
    this.fileRequired = fileRequired;
  }

//  public SpreadSheetType getFileType() {
//    return fileType;
//  }
//  public void setFileType(SpreadSheetType fileType) {
//    this.fileType = fileType;
//  }
  public String getFilenamePart() {
    return filenamePart;
  }




  public void setFilenamePart(String filenamePart) {
    this.filenamePart = filenamePart;
  }
  /**
   * @deprecated doesn't following naming standards
   */
  @Deprecated
  public boolean addResultFile() {
    return addresultfile;
  }

  /**
   * @return the add result filesname flag
   */
  public boolean getAddResultFile() {
    return addresultfile;
  }

  /**
   * @return the IsIgnoreEmptyFile flag
   */
  public boolean isIgnoreEmptyFile() {
    return IsIgnoreEmptyFile;
  }

  /**
   * @param IsIgnoreEmptyFile to set
   */
  public void setIgnoreEmptyFile( boolean IsIgnoreEmptyFile ) {
    this.IsIgnoreEmptyFile = IsIgnoreEmptyFile;
  }

  /**
   * @return the IsIgnoreMissingPath flag
   */
  public boolean isIgnoreMissingPath() {
    return IsIgnoreMissingPath;
  }

  /**
   * @param IsIgnoreMissingPath to set
   */
  public void setIgnoreMissingPath( boolean IsIgnoreMissingPath ) {
    this.IsIgnoreMissingPath = IsIgnoreMissingPath;
  }

  public void setAddResultFile( boolean addresultfile ) {
    this.addresultfile = addresultfile;
  }

  /**
   * @return Returns the input fields.
   */
  public ExcelContentInputField[] getInputFields() {
    return inputFields;
  }
//
  /**
   * @param inputFields
   *          The input fields to set.
   */
  public void setInputFields( ExcelContentInputField[] inputFields ) {
    this.inputFields = inputFields;
  }

  /************************************
   * get and set FilenameField
   *************************************/
  /**  */
//  public String getDynamicFilenameField() {
//    return DynamicFilenameField;
//  }
//
//  /**  */
//  public void setDynamicFilenameField( String DynamicFilenameField ) {
//    this.DynamicFilenameField = DynamicFilenameField;
//  }

  /************************************
   * get / set fileInFields
   *************************************/
  /**  */
  public boolean getFileInFields() {
    return fileinfield;
  }

  /************************************
   * @deprecated doesn't follow standard naming
   *************************************/
  @Deprecated
  public boolean getIsInFields() {
    return fileinfield;
  }

  /**
   * @deprecated doesn't follow standard naming 
   */
  @Deprecated
  public void setIsInFields( boolean IsInFields ) {
    this.fileinfield = IsInFields;
  }

  public void setFileInFields( boolean IsInFields ) {
    this.fileinfield = IsInFields;
  }

  /**
   * @return Returns the fileMask.
   */
  public String getFileMask() {
    return fileMask;
  }

  /**
   * @param fileMask
   *          The fileMask to set.
   */
  public void setFileMask( String fileMask ) {
    this.fileMask = fileMask;
  }

  /**
   * @return Returns the fileName.
   */
  public String getFileName() {
    return fileName;
  }

  public boolean getIncludeSubFolders() {
    return includeSubFolders;
  }

  public void setIncludeSubFolders( boolean includeSubFoldersin ) {
      this.includeSubFolders = includeSubFoldersin ;
  }

  public String getRequiredFilesCode( String tt ) {
    if ( tt == null ) {
      return RequiredFilesCode[0];
    }
    if ( tt.equals( RequiredFilesDesc[1] ) ) {
      return RequiredFilesCode[1];
    } else {
      return RequiredFilesCode[0];
    }
  }

  public String getRequiredFilesDesc( String tt ) {
    if ( tt == null ) {
      return RequiredFilesDesc[0];
    }
    if ( tt.equals( RequiredFilesCode[1] ) ) {
      return RequiredFilesDesc[1];
    } else {
      return RequiredFilesDesc[0];
    }
  }

  /**
   * @param fileName
   *          The fileName to set.
   */
  public void setFileName( String fileName ) {
    this.fileName = fileName;
  }

  /**
   * @return Returns the filenameField.
   */
  public String getFilenameField() {
    return filenameField;
  }

  /**
   * @param filenameField
   *          The filenameField to set.
   */
  public void setFilenameField( String filenameField ) {
    this.filenameField = filenameField;
  }

  /**
   * @return Returns the includeFilename.
   * @deprecated doesn't follow standard naming
   */
//  @Deprecated
//  public boolean includeFilename() {
//    return includeFilename;
//  }
//
//  /**
//   * @return Returns the includeFilename.
//   *
//   */
//  public boolean getIncludeFilename() {
//    return includeFilename;
//  }

  /**
   * @param includeFilename
   *          The includeFilename to set.
   */
//  public void setIncludeFilename( boolean includeFilename ) {
//    this.includeFilename = includeFilename;
//  }

  /**
   * @return Returns the includeRowNumber.
   * @deprecated doesn't follow standard naming
   */
//  @Deprecated
//  public boolean includeRowNumber() {
//    return includeRowNumber;
//  }
//
//  /**
//   * @return Returns the includeRowNumber.
//   */
//  public boolean getIncludeRowNumber() {
//    return includeRowNumber;
//  }

  /**
   * @param includeRowNumber
   *          The includeRowNumber to set.
   */
//  //public void setIncludeRowNumber( boolean includeRowNumber ) {
//    this.includeRowNumber = includeRowNumber;
//  }

  /**
   * @return Returns the rowLimit.
   */
  public String getTemplateFile() {
    return templateFile;
  }

  /**
   * @param templateFile
   *          The rowLimit to set.
   */
  public void setTemplateFile( String templateFile ) {
    this.templateFile = templateFile;
  }

  /**
   * @return Returns the rowNumberField.
   */
  //public String getRowNumberField() {
//    return rowNumberField;
//  }

  /**
   * @param rowNumberField
   *          The rowNumberField to set.
   */
//  public void setRowNumberField( String rowNumberField ) {
//    this.rowNumberField = rowNumberField;
//  }

  /**
   * @return the encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @param encoding
   *          the encoding to set
   */
  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    ExcelContentInputMeta retval = (ExcelContentInputMeta) super.clone();
//
//    int nrFiles = fileName.length;
    int nrFields = 4;
//
//    retval.allocate( nrFiles, nrFields );
//    System.arraycopy( fileName, 0, retval.fileName, 0, nrFiles );
//    System.arraycopy( fileMask, 0, retval.fileMask, 0, nrFiles );
//    System.arraycopy( excludeFileMask, 0, retval.excludeFileMask, 0, nrFiles );
//    System.arraycopy( fileRequired, 0, retval.fileRequired, 0, nrFiles );
//    System.arraycopy( includeSubFolders, 0, retval.includeSubFolders, 0, nrFiles );

    return retval;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " + XMLHandler.addTagValue( INCLUDE, fileinfield ) );
    retval.append( "    " + XMLHandler.addTagValue( INCLUDE_FIELD, filenameField ) );
    retval.append( "    " + XMLHandler.addTagValue( IS_IGNORE_EMPTY_FILE, IsIgnoreEmptyFile ) );
    retval.append( "    " + XMLHandler.addTagValue( ENCODING, encoding ) );
    retval.append( "    " + XMLHandler.addTagValue( TEMPLATE_FILE, templateFile ) );



    retval.append( "    <" + FILE + ">" + Const.CR );
    retval.append( "      " + XMLHandler.addTagValue( NAME, fileName ) );
    retval.append( "      " + XMLHandler.addTagValue(FILE_NAME_PART, this.filenamePart ) );
    retval.append( "      " + XMLHandler.addTagValue(FILE_TYPE, this.fileType.name() ) );
    retval.append( "      " + XMLHandler.addTagValue( INCLUDE_SUBFOLDERS, includeSubFolders ) );
    retval.append( "      </" + FILE + ">" + Const.CR );

    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      this.fileinfield = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, INCLUDE ) );
      filenameField = XMLHandler.getTagValue( stepnode, INCLUDE_FIELD );
      IsIgnoreEmptyFile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, IS_IGNORE_EMPTY_FILE ) );

      encoding = XMLHandler.getTagValue( stepnode, ENCODING );

      Node filenode = XMLHandler.getSubNode( stepnode, FILE );

      Node filenamenode = XMLHandler.getSubNodeByNr( filenode, NAME, 0 );
      Node filemasknode = XMLHandler.getSubNodeByNr( filenode, FILE_NAME_PART, 0 );
      Node includeSubFoldersnode = XMLHandler.getSubNodeByNr( filenode, INCLUDE_SUBFOLDERS, 0 );
      Node filetypenode = XMLHandler.getSubNodeByNr( filenode, FILE_TYPE, 0 );

      fileName = XMLHandler.getNodeValue( filenamenode );
      fileMask = XMLHandler.getNodeValue( filemasknode );
      includeSubFolders = YES.equals(XMLHandler.getNodeValue( includeSubFoldersnode ));
      //fileType = XMLHandler.getNodeValue( filetypenode );
      fileType = SpreadSheetType.POI;
      //allocate(0,4);
      // Is there a limit on the number of rows we process?
      templateFile =  XMLHandler.getTagValue( stepnode, TEMPLATE_FILE);
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "LoadFileInputMeta.Exception.ErrorLoadingXML", e
          .toString() ) );
    }
  }
//
  public void allocate( int nrfiles, int nrfields ) {
//    fileName = new String[nrfiles];
//    fileMask = new String[nrfiles];
//    excludeFileMask = new String[nrfiles];
//    fileRequired = new String[nrfiles];
//    includeSubFolders = new String[nrfiles];
//    inputFields = new FileContentInputField[nrfields];
//
  }

  public void setDefault() {
    fileName = "";
    fileMask = "";
    excludeFileMask = "";
    includeSubFolders = false;
    templateFile = "";
    fileinfield = false;
    //DynamicFilenameField = null;

    encoding = "UTF-8";
    IsIgnoreEmptyFile = false;
    //includeFilename = false;
    filenameField = "";
    //includeRowNumber = false;
    //rowNumberField = "";
    addresultfile = true;
  }

  /**
   * return a hashmap of ValueMetaInterface
   * @param origineName
   * @return
   */
  public HashMap<String,String> getFieldFromTemplateFile(String origineName)
  {
    KWorkbook workbook = null;
    String content;
    HashMap r = new HashMap<String,String>();
    try {
      workbook = WorkbookFactory.getWorkbook( SpreadSheetType.POI , this.getTemplateFile(), this.getEncoding(), "" );
      KSheet sheet = workbook.getSheet(0);
      for(int i=0;i<100;i++) {
        for(int j=0;j<100;j++) {
          if(sheet.getCell(i, j)==null)
            continue;
          content = sheet.getCell(i, j).getContents();
          if(Utils.isEmpty(content))
            continue;
          if (content.startsWith("${") && content.endsWith("}"))
          {
            String fieldName = content.substring(2,content.length()-1);
            r.put( new Integer(j).toString()+"-"+new Integer(i).toString(),fieldName );
          }
        }
      }

    } catch (KettleException e) {
      throw new RuntimeException(e);
    }
    return r;
  }

  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( !getIsInFields() ) {
      r.clear();
    }
    HashMap<String, String> fieldsList = getFieldFromTemplateFile(name);
    Set set =fieldsList.keySet();
    Iterator it = set.iterator();
    while(it.hasNext())
    {
      String fieldKey = it.next().toString();// key: row-column
      String fieldName = fieldsList.get(fieldKey);
      ValueMetaInterface v1 = new ValueMetaString( fieldName );
      v1.setLength( 255, -1 );
      v1.setOrigin( name );
      r.addValueMeta(v1);
    }
//    for ( i = 0; i < inputFields.length; i++ ) {
//      FileContentInputField field = inputFields[i];
//      int type = field.getType();
//
//      switch ( field.getElementType() ) {
//        case FileContentInputField.ELEMENT_TYPE_FILECONTENT:
//          if ( type == ValueMetaInterface.TYPE_NONE ) {
//            type = ValueMetaInterface.TYPE_STRING;
//          }
//          break;
//        case FileContentInputField.ELEMENT_TYPE_FILESIZE:
//          if ( type == ValueMetaInterface.TYPE_NONE ) {
//            type = ValueMetaInterface.TYPE_INTEGER;
//          }
//          break;
//        default:
//          break;
//      }
//
//      try {
//        ValueMetaInterface v = ValueMetaFactory.createValueMeta( space.environmentSubstitute( field.getName() ), type );
//        v.setLength( field.getLength() );
//        v.setPrecision( field.getPrecision() );
//        v.setConversionMask( field.getFormat() );
//        v.setCurrencySymbol( field.getCurrencySymbol() );
//        v.setDecimalSymbol( field.getDecimalSymbol() );
//        v.setGroupingSymbol( field.getGroupSymbol() );
//        v.setTrimType( field.getTrimType() );
//        v.setOrigin( name );
//        r.addValueMeta( v );
//      } catch ( Exception e ) {
//        throw new KettleStepException( e );
//      }
 //   }
//    if ( includeFilename ) {
//      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( filenameField ) );
//      v.setLength( 250 );
//      v.setPrecision( -1 );
//      v.setOrigin( name );
//      r.addValueMeta( v );
//    }
//    if ( includeRowNumber ) {
//      ValueMetaInterface v = new ValueMetaInteger( space.environmentSubstitute( rowNumberField ) );
//      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
//      v.setOrigin( name );
//      r.addValueMeta( v );
//    }
    // Add additional fields

  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      this.fileinfield = rep.getStepAttributeBoolean( id_step, INCLUDE );
      filenameField = rep.getStepAttributeString( id_step, INCLUDE_FIELD );
      addresultfile = rep.getStepAttributeBoolean( id_step, ADDRESULTFILE );
      IsIgnoreEmptyFile = rep.getStepAttributeBoolean( id_step, IS_IGNORE_EMPTY_FILE );
      IsIgnoreMissingPath = rep.getStepAttributeBoolean( id_step, IS_IGNORE_MISSING_PATH );

//      includeRowNumber = rep.getStepAttributeBoolean( id_step, ROWNUM );
//      rowNumberField = rep.getStepAttributeString( id_step, ROWNUM_FIELD );

      fileName = rep.getStepAttributeString( id_step,  FILE_NAME_REP );
      fileMask = rep.getStepAttributeString( id_step,  FILE_MASK_REP );
      fileType = SpreadSheetType.valueOf(rep.getStepAttributeString( id_step,  FILE_TYPE ));
      filenamePart= rep.getStepAttributeString( id_step,  FILE_NAME_PART );
      includeSubFolders = rep.getStepAttributeBoolean( id_step,  INCLUDE_SUBFOLDERS );

      templateFile = rep.getStepAttributeString( id_step, TEMPLATE_FILE);
      encoding = rep.getStepAttributeString( id_step, ENCODING );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
              "LoadFileInputMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, INCLUDE, fileinfield );
      rep.saveStepAttribute( id_transformation, id_step, INCLUDE_FIELD, filenameField );
      //rep.saveStepAttribute( id_transformation, id_step, ADDRESULTFILE, addresultfile );
      rep.saveStepAttribute( id_transformation, id_step, IS_IGNORE_EMPTY_FILE, IsIgnoreEmptyFile );
      //rep.saveStepAttribute( id_transformation, id_step, IS_IGNORE_MISSING_PATH, IsIgnoreMissingPath );

      rep.saveStepAttribute( id_transformation, id_step, TEMPLATE_FILE, templateFile );
      rep.saveStepAttribute( id_transformation, id_step, ENCODING, encoding );


      rep.saveStepAttribute( id_transformation, id_step,  FILE_NAME_REP, fileName );
      rep.saveStepAttribute( id_transformation, id_step,  FILE_MASK_REP, fileMask );
      rep.saveStepAttribute( id_transformation, id_step,  FILE_TYPE, fileType.name() );
      rep.saveStepAttribute( id_transformation, id_step,  FILE_NAME_PART, filenamePart );
      rep.saveStepAttribute( id_transformation, id_step,  INCLUDE_SUBFOLDERS, includeSubFolders );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "LoadFileInputMeta.Exception.ErrorSavingToRepository", ""
          + id_step ), e );
    }
  }

  public FileInputList getFiles( VariableSpace space ) throws KettleFileException, FileSystemException {
    FileInputList list =null;
    if(KettleVFS.getFileObject(fileName).isFolder())
    {
      String mask = createFileMask();
      list=  this.createFileList( space, new String[]{fileName}, new String[]{mask}, new String[]{excludeFileMask}, new String[]{"YES"},
              includeSubFolderBoolean(),null );
    }
      else
      list = this.createFileList( space, new String[]{fileName}, new String[]{fileMask}, new String[]{excludeFileMask}, new String[]{"YES"},
        includeSubFolderBoolean(),null );
    return list;
  }

  public  FileInputList createFileList( VariableSpace space, String[] fileName, String[] fileMask,
                                              String[] excludeFileMask, String[] fileRequired, boolean[] includeSubdirs,
                                              FileInputList.FileTypeFilter[] fileTypeFilters ) throws FileSystemException, KettleFileException {
    FileInputList fileInputList = new FileInputList();

    // Replace possible environment variables...
    final String[] realfile = space.environmentSubstitute( fileName );
    final String[] realmask = space.environmentSubstitute( fileMask );
    final String[] realExcludeMask = space.environmentSubstitute( excludeFileMask );

    for ( int i = 0; i < realfile.length; i++ ) {
      final String onefile = realfile[ i ];
      final String onemask = realmask[ i ];
      final String excludeonemask = realExcludeMask[ i ];
      final boolean onerequired = YES.equalsIgnoreCase( fileRequired[ i ] );
      final boolean subdirs = includeSubdirs[ i ];
      final FileInputList.FileTypeFilter filter =
              ( ( fileTypeFilters == null || fileTypeFilters[ i ] == null )
                      ? FileInputList.FileTypeFilter.ONLY_FILES : fileTypeFilters[ i ] );

      if ( Utils.isEmpty( onefile ) ) {
        continue;
      }

      //
      // If a wildcard is set we search for files
      //
      if ( !Utils.isEmpty( onemask ) || !Utils.isEmpty( excludeonemask ) ) {

          FileObject directoryFileObject = KettleVFS.getFileObject( onefile, space );
          boolean processFolder = true;
          if ( onerequired ) {
            if ( !directoryFileObject.exists() ) {
              // if we don't find folder..no need to continue
              fileInputList.addNonExistantFile( directoryFileObject );
              processFolder = false;
            } else {
              if ( !directoryFileObject.isReadable() ) {
                fileInputList.addNonAccessibleFile( directoryFileObject );
                processFolder = false;
              }
            }
          }
          // Find all file names that match the wildcard in this directory
        if ( processFolder ) {
          if ( directoryFileObject != null && directoryFileObject.getType() == FileType.FOLDER ) { // it's a directory
            FileObject[] fileObjects = directoryFileObject.findFiles( new AllFileSelector() {
              @Override
              public boolean traverseDescendents( FileSelectInfo info ) {
                return info.getDepth() == 0 || subdirs;
              }

              @Override
              public boolean includeFile( FileSelectInfo info ) {
                // Never return the parent directory of a file list.
                if ( info.getDepth() == 0 ) {
                  return false;
                }

                FileObject fileObject = info.getFile();
                try {
                  if ( fileObject != null && filter.isFileTypeAllowed( fileObject.getType() ) ) {
                    String name = info.getFile().getName().getBaseName();
                    boolean matches = true;
                    if ( !Utils.isEmpty( onemask ) ) {
                      matches = Pattern.matches( onemask, name );
                    }
                    boolean excludematches = false;
                    if ( !Utils.isEmpty( excludeonemask ) ) {
                      excludematches = Pattern.matches( excludeonemask, name );
                    }
                    return ( matches && !excludematches );
                  }
                  return false;
                } catch ( IOException ex ) {
                  // Upon error don't process the file.
                  return false;
                }
              }
            } );
            if ( fileObjects != null ) {
              for ( int j = 0; j < fileObjects.length; j++ ) {
                FileObject fileObject = fileObjects[ j ];
                if ( fileObject.exists() ) {
                  fileInputList.addFile( fileObject );
                }
              }
            }
            if ( Utils.isEmpty( fileObjects ) ) {
              if ( onerequired ) {
                fileInputList.addNonAccessibleFile( directoryFileObject );
              }
            }

            // Sort the list: quicksort, only for regular files
            fileInputList.sortFiles();
          } else {
            FileObject[] children = directoryFileObject.getChildren();
            for ( int j = 0; j < children.length; j++ ) {
              // See if the wildcard (regexp) matches...
              String name = children[ j ].getName().getBaseName();
              boolean matches = true;
              if ( !Utils.isEmpty( onemask ) ) {
                matches = Pattern.matches( onemask, name );
              }
              boolean excludematches = false;
              if ( !Utils.isEmpty( excludeonemask ) ) {
                excludematches = Pattern.matches( excludeonemask, name );
              }
              if ( matches && !excludematches ) {
                fileInputList.addFile( children[ j ] );
              }

            }
            // We don't sort here, keep the order of the files in the archive.
          }
        }
      } else { // A normal file...

        try {
          FileObject fileObject = KettleVFS.getFileObject( onefile, space );
          if ( fileObject.exists() ) {
            if ( fileObject.isReadable() ) {
              fileInputList.addFile( fileObject );
            } else {
              if ( onerequired ) {
                fileInputList.addNonAccessibleFile( fileObject );
              }
            }
          } else {
            if ( onerequired ) {
              fileInputList.addNonExistantFile( fileObject );
            }
          }
        } catch ( Exception e ) {
          if ( onerequired ) {
            fileInputList.addNonAccessibleFile( new NonAccessibleFileObject( onefile ) );
          }
          log.logError( Const.getStackTracker( e ) );
        }
      }
    }

    return fileInputList;
  }

  /**
   * mask like this:  .*\.(txt|doc|docx|pdf)$
   * @return
   */
  private String createFileMask() {
    String mask="";
    if(!Utils.isEmpty(getFilenamePart()))
    {
      mask=".*"+getFilenamePart()+".*";
    }else
    {
      mask=".*";
    }
    mask+="\\."+"(";
    mask+="xls|xlsx";
    mask+=")";
    return mask;
  }

  private boolean[] includeSubFolderBoolean() {
    boolean[] includeSubFolderBoolean = new boolean[1];
    includeSubFolderBoolean[0] =  includeSubFolders;
    return includeSubFolderBoolean;
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;

    if ( getIsInFields() ) {
      // See if we get input...
      if ( input.length == 0 ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "LoadFileInputMeta.CheckResult.NoInputExpected" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "LoadFileInputMeta.CheckResult.NoInput" ), stepMeta );
        remarks.add( cr );
      }

//      if ( Utils.isEmpty( getDynamicFilenameField() ) ) {
//        cr =
//            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
//                "LoadFileInputMeta.CheckResult.NoField" ), stepMeta );
//        remarks.add( cr );
//      } else {
//        cr =
//            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
//                "LoadFileInputMeta.CheckResult.FieldOk" ), stepMeta );
//        remarks.add( cr );
//      }
    } else {
      FileInputList fileInputList = null;
      try {
        fileInputList = getFiles( transMeta );
      } catch (KettleFileException e) {
        throw new RuntimeException(e);
      } catch (FileSystemException e) {
        throw new RuntimeException(e);
      }

      if ( fileInputList == null || fileInputList.getFiles().size() == 0 ) {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "LoadFileInputMeta.CheckResult.NoFiles" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "LoadFileInputMeta.CheckResult.FilesOk", "" + fileInputList.getFiles().size() ), stepMeta );
        remarks.add( cr );
      }
    }
  }

  /**
   * @param space
   *          the variable space to use
   * @param definitions
   * @param resourceNamingInterface
   * @param repository
   *          The repository to optionally load other resources from (to be converted to XML)
   * @param metaStore
   *          the metaStore in which non-kettle metadata could reside.
   *
   * @return the filename of the exported resource
   */
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
      ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      //
      if ( !fileinfield ) {
        FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( fileName ), space );
        fileName = resourceNamingInterface.nameResource( fileObject, space, Utils.isEmpty( fileMask ) );
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans ) {
    return new ExcelContentInput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new ExcelContentInputData();
  }

  public boolean supportsErrorHandling() {
    return true;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof ExcelContentInputMeta) ) {
      return false;
    }
    ExcelContentInputMeta that = (ExcelContentInputMeta) o;

    if ( IsIgnoreEmptyFile != that.IsIgnoreEmptyFile ) {
      return false;
    }
    if ( IsIgnoreMissingPath != that.IsIgnoreMissingPath ) {
      return false;
    }
    if ( addresultfile != that.addresultfile ) {
      return false;
    }
    if ( fileinfield != that.fileinfield ) {
      return false;
    }
//    if ( includeFilename != that.includeFilename ) {
//      return false;
//    }
//    if ( includeRowNumber != that.includeRowNumber ) {
//      return false;
//    }
    if ( templateFile != that.templateFile ) {
      return false;
    }
//    if ( DynamicFilenameField != null ? !DynamicFilenameField.equals( that.DynamicFilenameField )
//        : that.DynamicFilenameField != null ) {
//      return false;
//    }
    if ( encoding != null ? !encoding.equals( that.encoding ) : that.encoding != null ) {
      return false;
    }
    if ( !( excludeFileMask.equals(excludeFileMask) ) ) {
      return false;
    }
    if ( extensionFieldName != null ? !extensionFieldName.equals( that.extensionFieldName )
        : that.extensionFieldName != null ) {
      return false;
    }
    if ( !fileMask.equals(that.fileMask )) {
      return false;
    }
    if ( !fileName.equals(that.fileName )) {
      return false;
    }
    if ( !Arrays.equals( fileRequired, that.fileRequired ) ) {
      return false;
    }
    if ( filenameField != null ? !filenameField.equals( that.filenameField ) : that.filenameField != null ) {
      return false;
    }
    if ( hiddenFieldName != null ? !hiddenFieldName.equals( that.hiddenFieldName ) : that.hiddenFieldName != null ) {
      return false;
    }
    if ( includeSubFolders!=that.includeSubFolders  ) {
      return false;
    }
    if ( !Arrays.equals( inputFields, that.inputFields ) ) {
      return false;
    }
    if ( lastModificationTimeFieldName != null ? !lastModificationTimeFieldName
        .equals( that.lastModificationTimeFieldName ) : that.lastModificationTimeFieldName != null ) {
      return false;
    }
    if ( pathFieldName != null ? !pathFieldName.equals( that.pathFieldName ) : that.pathFieldName != null ) {
      return false;
    }
    if ( rootUriNameFieldName != null ? !rootUriNameFieldName.equals( that.rootUriNameFieldName )
        : that.rootUriNameFieldName != null ) {
      return false;
    }
//    if ( rowNumberField != null ? !rowNumberField.equals( that.rowNumberField ) : that.rowNumberField != null ) {
//      return false;
//    }
    if ( shortFileFieldName != null ? !shortFileFieldName.equals( that.shortFileFieldName )
        : that.shortFileFieldName != null ) {
      return false;
    }
    return !( uriNameFieldName != null ? !uriNameFieldName.equals( that.uriNameFieldName )
        : that.uriNameFieldName != null );

  }

  @Override
  public int hashCode() {
    int result = fileName != null ?  fileName.hashCode() : 0;
    result = 31 * result + ( fileMask != null ?  fileMask.hashCode() : 0 );
    result = 31 * result + ( excludeFileMask != null ? excludeFileMask.hashCode() : 0 );
    //result = 31 * result + ( includeFilename ? 1 : 0 );
    result = 31 * result + ( filenameField != null ? filenameField.hashCode() : 0 );
    //result = 31 * result + ( includeRowNumber ? 1 : 0 );
    //result = 31 * result + ( rowNumberField != null ? rowNumberField.hashCode() : 0 );
    result = 31 * result + (templateFile != null ? templateFile.hashCode() : 0 );
    result = 31 * result + ( inputFields != null ? Arrays.hashCode( inputFields ) : 0 );
    result = 31 * result + ( encoding != null ? encoding.hashCode() : 0 );
    //result = 31 * result + ( DynamicFilenameField != null ? DynamicFilenameField.hashCode() : 0 );
    result = 31 * result + ( fileinfield ? 1 : 0 );
    result = 31 * result + ( addresultfile ? 1 : 0 );
    result = 31 * result + ( fileRequired != null ? Arrays.hashCode( fileRequired ) : 0 );
    result = 31 * result + ( IsIgnoreEmptyFile ? 1 : 0 );
    result = 31 * result + ( IsIgnoreMissingPath ? 1 : 0 );
    result = 31 * result + ( includeSubFolders?1 : 0 );
    result = 31 * result + ( shortFileFieldName != null ? shortFileFieldName.hashCode() : 0 );
    result = 31 * result + ( pathFieldName != null ? pathFieldName.hashCode() : 0 );
    result = 31 * result + ( hiddenFieldName != null ? hiddenFieldName.hashCode() : 0 );
    result = 31 * result + ( lastModificationTimeFieldName != null ? lastModificationTimeFieldName.hashCode() : 0 );
    result = 31 * result + ( uriNameFieldName != null ? uriNameFieldName.hashCode() : 0 );
    result = 31 * result + ( rootUriNameFieldName != null ? rootUriNameFieldName.hashCode() : 0 );
    result = 31 * result + ( extensionFieldName != null ? extensionFieldName.hashCode() : 0 );
    return result;
  }
}
