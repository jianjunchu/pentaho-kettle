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

package org.pentaho.di.trans.steps.excelcontentinput;

import java.io.*;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.pentaho.di.trans.steps.excelinput.SpreadSheetType;
import org.pentaho.di.trans.steps.excelinput.WorkbookFactory;

/**
 * Read files, parse them and convert them to rows and writes these to one or more output streams.
 *
 * @author Samatar
 * @since 20-06-2007
 */
public class ExcelContentInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = ExcelContentInputMeta.class; // for i18n purposes, needed by Translator2!!

  ExcelContentInputMeta meta;
  ExcelContentInputData data;

  public ExcelContentInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                           Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private void addFileToResultFilesName( FileObject file ) throws Exception {
    if ( meta.getAddResultFile() ) {
      // Add this to the result file names...
      ResultFile resultFile =
        new ResultFile( ResultFile.FILE_TYPE_GENERAL, file, getTransMeta().getName(), getStepname() );
      resultFile.setComment( "File was read by a LoadFileInput step" );
      addResultFile( resultFile );
    }
  }

   public FileObject getNextFile() {
    try {
      if ( meta.getFileInFields() ) {
        data.readrow = getRow(); // Grab another row ...

        if ( data.readrow == null ) { // finished processing!
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "LoadFileInput.Log.FinishedProcessing" ) );
          }
          return null;
        }
        if ( first ) {
          first = false;
          data.inputRowMeta = getInputRowMeta();
          data.outputRowMeta = data.inputRowMeta.clone();
          meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
          // Create convert meta-data objects that will contain Date & Number formatters
          // All non binary content is handled as a String. It would be converted to the target type after the processing.
          data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );
            // Check is filename field is provided
          if ( Utils.isEmpty( meta.getFilenameField() ) ) {
            logError( BaseMessages.getString( PKG, "LoadFileInput.Log.NoField" ) );
            throw new KettleException( BaseMessages.getString( PKG, "LoadFileInput.Log.NoField" ) );
          }
          // cache the position of the field
          if ( data.indexOfFilenameField < 0 ) {
            data.indexOfFilenameField = data.inputRowMeta.indexOfValue( meta.getFilenameField() );
            if ( data.indexOfFilenameField < 0 ) {
              // The field is unreachable !
              logError( BaseMessages.getString( PKG, "LoadFileInput.Log.ErrorFindingField" )
                + "[" + meta.getFilenameField() + "]" );
              throw new KettleException( BaseMessages.getString(
                PKG, "LoadFileInput.Exception.CouldnotFindField", meta.getFilenameField() ) );
            }
          }
          // Get the number of previous fields
          data.totalpreviousfields = data.inputRowMeta.size();

        } // end if first

        // get field value
        String Fieldvalue = data.inputRowMeta.getString( data.readrow, data.indexOfFilenameField );
        try {
          // Source is a file.
          data.file = KettleVFS.getFileObject( Fieldvalue );
        } catch ( Exception e ) {
          throw new KettleException( e );
        }
      } else {
        if ( data.filenr >= data.files.nrOfFiles() ) {
          // finished processing!
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "LoadFileInput.Log.FinishedProcessing" ) );
          }
          return null;
        }
        // Is this the last file?
        data.last_file = ( data.filenr == data.files.nrOfFiles() - 1 );
        data.file = data.files.getFile( data.filenr );
      }
      // Check if file exists
      if ( meta.isIgnoreMissingPath() && !data.file.exists() ) {
        logBasic( BaseMessages.getString( PKG, "LoadFileInput.Error.FileNotExists", "" + data.file.getName() ) );
        return getNextFile();
      }
      // Check if file is empty
      data.fileSize = data.file.getContent().getSize();
      // Move file pointer ahead!
      data.filenr++;
      if ( meta.isIgnoreEmptyFile() && data.fileSize == 0 ) {
        logError( BaseMessages.getString( PKG, "LoadFileInput.Error.FileSizeZero", "" + data.file.getName() ) );
        return getNextFile();

      } else {
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "LoadFileInput.Log.OpeningFile", data.file.toString() ) );
        }
        data.filename = KettleVFS.getFilename( data.file );
        if(data.filename.indexOf("/")>-1)
          data.shortFilename =data.filename.substring(data.filename.lastIndexOf("/")+1,data.filename.length());
        else if(data.filename.indexOf("\\")>-1)
          data.shortFilename = data.filename.substring(data.filename.lastIndexOf("\\")+1,data.filename.length());
        else
          data.shortFilename ="";

        // Add additional fields?
        if ( meta.getShortFileNameField() != null && meta.getShortFileNameField().length() > 0 ) {
          data.shortFilename = data.file.getName().getBaseName();
        }
        if ( meta.getPathField() != null && meta.getPathField().length() > 0 ) {
          data.path = KettleVFS.getFilename( data.file.getParent() );
        }
        if ( meta.isHiddenField() != null && meta.isHiddenField().length() > 0 ) {
          data.hidden = data.file.isHidden();
        }
        if ( meta.getExtensionField() != null && meta.getExtensionField().length() > 0 ) {
          data.extension = data.file.getName().getExtension();
        }
        if ( meta.getLastModificationDateField() != null && meta.getLastModificationDateField().length() > 0 ) {
          data.lastModificationDateTime = new Date( data.file.getContent().getLastModifiedTime() );
        }
        if ( meta.getUriField() != null && meta.getUriField().length() > 0 ) {
          data.uriName = data.file.getName().getURI();
        }
        if ( meta.getRootUriField() != null && meta.getRootUriField().length() > 0 ) {
          data.rootUriName = data.file.getName().getRootURI();
        }
        // get File content
        //getFileContent();
        addFileToResultFilesName( data.file );

        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "LoadFileInput.Log.FileOpened", data.file.toString() ) );
        }
      }

    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "LoadFileInput.Log.UnableToOpenFile", "" + data.filenr, data.file
        .toString(), e.toString() ) );
      //stopAll(); //not stop when error
      setErrors( 1 );
      //return false;
    }
    return data.file;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    try {
      // Grab a row
      Object[] outputRowData = getOneRow();
      if ( outputRowData == null ) {
        setOutputDone(); // signal end to receiver(s)
        return false; // end of data or error.
      }

      if ( isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "LoadFileInput.Log.ReadRow", data.outputRowMeta
          .getString( outputRowData ) ) );
      }

      putRow( data.outputRowMeta, outputRowData );

    } catch ( KettleException e ) {
      logError( BaseMessages.getString( PKG, "LoadFileInput.ErrorInStepRunning", e.getMessage() ) );
      logError( Const.getStackTracker( e ) );
      setErrors( 1 );
      stopAll();
      setOutputDone(); // signal end to receiver(s)
      return false;
    }
    return true;
  }

  void getFileContent() throws KettleException {
    try {
      //data.filecontent = getFileBinaryContent( data.file.toString() );
      String fileName = data.file.getName().getPathDecoded();
      data.filecontent =null;
    } catch ( OutOfMemoryError o ) {
      logError( "There is no enaugh memory to load the content of the file [" + data.file.getName() + "]" );
      throw new KettleException( o );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  private void handleMissingFiles() throws KettleException {
    List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();

    if ( nonExistantFiles.size() != 0 ) {
      String message = FileInputList.getRequiredFilesDescription( nonExistantFiles );
      logError( BaseMessages.getString( PKG, "LoadFileInput.Log.RequiredFilesTitle" ), BaseMessages.getString(
        PKG, "LoadFileInput.Log.RequiredFiles", message ) );

      throw new KettleException( BaseMessages.getString( PKG, "LoadFileInput.Log.RequiredFilesMissing", message ) );
    }

    List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
    if ( nonAccessibleFiles.size() != 0 ) {
      String message = FileInputList.getRequiredFilesDescription( nonAccessibleFiles );
      logError( BaseMessages.getString( PKG, "LoadFileInput.Log.RequiredFilesTitle" ), BaseMessages.getString(
        PKG, "LoadFileInput.Log.RequiredNotAccessibleFiles", message ) );
      throw new KettleException( BaseMessages.getString(
        PKG, "LoadFileInput.Log.RequiredNotAccessibleFilesMissing", message ) );
    }
  }

  /**
   * Build an empty row based on the meta-data...
   *
   * @return
   */

  private Object[] buildEmptyRow() {
    Object[] rowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );

    return rowData;
  }

  Object[] getOneRow() throws KettleException {
    if ( getNextFile()==null ) {
      return null;
    }

    // Build an empty row based on the meta-data
    Object[] outputRowData = buildEmptyRow();

    try {
      // Create new row or clone
      if ( meta.getIsInFields() ) {
        outputRowData = copyOrCloneArrayFromLoadFile( outputRowData, data.readrow );
      }


      KWorkbook workbook = null;
      int rowIndex = 0;

      try {
        workbook = WorkbookFactory.getWorkbook( SpreadSheetType.POI , data.file.getName().toString(), meta.getEncoding(), "" );
        String content;
        KSheet sheet = workbook.getSheet(0);
        for(int i=0;i<100;i++) {
          for(int j=0;j<100;j++) {
            if(sheet.getCell(i, j)==null)
              continue;
            content = sheet.getCell(i, j).getContents();
            if(Utils.isEmpty(content))
              continue;
            if (data.fieldsMap.containsKey(new Integer(j).toString()+"-"+new Integer(i).toString() ))
            {
              outputRowData[rowIndex++] =content;
            }
          }
        }

      } catch (KettleException e) {
        throw new RuntimeException(e);
      }


//      // Read fields...
//      for ( int i = 0; i < data.nrInputFields; i++ ) {
//        // Get field
//        ExcelContentInputField loadFileInputField = meta.getInputFields()[i];
//
//        Object o = null;
//        int indexField = data.totalpreviousfields + i;
//        ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta( indexField );
//        ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta( indexField );
//
//        switch ( loadFileInputField.getElementType() ) {
//          case ExcelContentInputField.ELEMENT_TYPE_FILECONTENT:
//            // DO Trimming!
//            switch ( loadFileInputField.getTrimType() ) {
//              case ExcelContentInputField.TYPE_TRIM_LEFT:
//                 data.filecontent = Const.ltrim( data.filecontent );
//                break;
//              case ExcelContentInputField.TYPE_TRIM_RIGHT:
//                data.filecontent = Const.rtrim( data.filecontent );
//                break;
//              case ExcelContentInputField.TYPE_TRIM_BOTH:
//                data.filecontent = Const.trim( data.filecontent );
//                break;
//              default:
//                break;
//            }
//            // save as byte[] without any conversion
//            o = data.filecontent;
//            break;
//          case ExcelContentInputField.ELEMENT_TYPE_FILESIZE:
//            o = String.valueOf( data.fileSize );
//            break;
//          default:
//            break;
//        }
//
//        if ( targetValueMeta.getType() == ValueMetaInterface.TYPE_BINARY ) {
//          // save as byte[] without any conversion
//          outputRowData[indexField] = o;
//        } else {
//          // convert string (processing type) to the target type
//          outputRowData[indexField] = targetValueMeta.convertData( sourceValueMeta, o );
//        }
//      } // End of loop over fields...

      // See if we need to add the filename to the row...
//      outputRowData[rowIndex++] = data.filecontent;
//      outputRowData[rowIndex++] = data.fileSize;
//      outputRowData[rowIndex++] = data.filename;
//      outputRowData[rowIndex++] = data.shortFilename;
      data.rownr++;
    } catch ( Exception e ) {
      throw new KettleException( "Error during processing a row", e );
    }

    return outputRowData;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExcelContentInputMeta) smi;
    data = (ExcelContentInputData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( !meta.getIsInFields() ) {
        try {
          data.files = meta.getFiles( this );
          handleMissingFiles();
          // Create the output row meta-data
          data.outputRowMeta = new RowMeta();
          HashMap<String,String> fieldsMap = meta.getFieldFromTemplateFile(getStepname());
          Set set =fieldsMap.keySet();
          Iterator it = set.iterator();
          while(it.hasNext())
          {
            String fieldKey = it.next().toString();// key: row-column
            String fieldName = fieldsMap.get(fieldKey);
            ValueMetaInterface v1 = new ValueMetaString( fieldName );
            v1.setLength( 255, -1 );
            v1.setOrigin( getStepname() );
            data.outputRowMeta.addValueMeta(v1);
          }
          data.fieldsMap =  fieldsMap;

          //meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore ); // get the metadata populated

          // Create convert meta-data objects that will contain Date & Number formatters
          // All non binary content is handled as a String. It would be converted to the target type after the processing.
          data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );
        } catch ( Exception e ) {
          logError( "Error at step initialization: " + e.toString() );
          logError( Const.getStackTracker( e ) );
          return false;
        }
      }
      data.rownr = 1L;
      //data.nrInputFields = meta.getInputFields().length;

      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExcelContentInputMeta) smi;
    data = (ExcelContentInputData) sdi;
    if ( data.file != null ) {
      try {
        data.file.close();
      } catch ( Exception e ) {
        // Ignore errors
      }
    }
    super.dispose( smi, sdi );
  }

  protected Object[] copyOrCloneArrayFromLoadFile( Object[] outputRowData, Object[] readrow ) {
    // if readrow array is shorter than outputRowData reserved space, then we can not clone it because we have to
    // preserve the outputRowData reserved space. Clone, creates a new array with a new length, equals to the
    // readRow length and with that set we lost our outputRowData reserved space - needed for future additions.
    // The equals case works in both clauses, but arraycopy is up to 5 times faster for smaller arrays.
    if ( readrow.length <= outputRowData.length ) {
      System.arraycopy( readrow, 0, outputRowData, 0, readrow.length );
    } else {
      // if readrow array is longer than outputRowData reserved space, then we can only clone it.
      // Copy does not work here and will return an error since we are trying to copy a bigger array into a shorter one.
      outputRowData = readrow.clone();
    }
    return outputRowData;
  }

}
