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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleConversionException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMeta;
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
import org.pentaho.di.trans.steps.textfileinput.EncodingType;

import java.io.File;
import java.io.InputStream;
import java.util.function.Consumer;

import com.aspose.words.Cell;
import com.aspose.words.CellCollection;
import com.aspose.words.Document;
import com.aspose.words.License;
import com.aspose.words.NodeType;
import com.aspose.words.Row;
import com.aspose.words.SaveFormat;
import com.aspose.words.Table;

import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.poi.hslf.usermodel.HSLFTableCell;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.AbstractWordUtils;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.model.StyleDescription;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableProperties;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

/**
 * Read a Word file
 * Just output table data found in the file...
 * 
 * @author Matt
 * @since 2007-07-05
 */
public class WordInput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = WordInput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private WordInputMeta meta;
	private WordInputData data;
	
	public WordInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(WordInputMeta)smi;
		data=(WordInputData)sdi;

		if (first) {
			first=false;
			
			data.outputRowMeta = new RowMeta();
			if(meta.extractTable())
				meta.getTableFields(data.outputRowMeta, getStepname(), null, null, this);
			else
				meta.getFixedFields(data.outputRowMeta, getStepname(), null, null, this);
			if (data.filenames==null) {
				// We're expecting the list of filenames from the previous step(s)...
				//
				getFilenamesFromPreviousSteps();
			}

			
			// The conversion logic for when the lazy conversion is turned of is simple:
			// Pretend it's a lazy conversion object anyway and get the native type during conversion.
			//
			data.convertRowMeta = data.outputRowMeta.clone();
			for (ValueMetaInterface valueMeta : data.convertRowMeta.getValueMetaList())
			{
				valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
			}
			
			// Calculate the indexes for the filename and row number fields
			//
			data.filenameFieldIndex = -1;
			if (!Const.isEmpty(meta.getFilenameField()) && meta.isIncludingFilename()) {
				data.filenameFieldIndex = meta.getInputFields().length;
			}
			
			data.rownumFieldIndex = -1;
			if (!Const.isEmpty(meta.getRowNumField())) {
				data.rownumFieldIndex = meta.getInputFields().length;
				if (data.filenameFieldIndex>=0) {
					data.rownumFieldIndex++;
				}
			}
			
//			// Now handle the parallel reading aspect: determine total of all the file sizes
//			// Then skip to the appropriate file and location in the file to start reading...
//			// Also skip to right after the first newline
//			//
//			if (data.parallel) {
//				prepareToRunInParallel();
//			}
			
			// Open the next file...
			//
			if (!openNextFile()) {
				setOutputDone();
				return false; // nothing to see here, move along...
			}	
		}

		try {
			Object[] outputRowData= null;
			if(meta.extractTable())
				outputRowData =readOneRow(true);    // get row, set busy!
			else
				outputRowData= readOneJsonRow();
			if (outputRowData==null)  // no more input to be expected...
			{
				if (openNextFile()) {
					return true; // try again on the next loop...
				}
				else {
					setOutputDone(); // last file, end here
					return false;
				}
			}
			else 
			{
				putRow(data.outputRowMeta, outputRowData);     // copy row to possible alternate rowset(s).
		        if (checkFeedback(getLinesInput())) 
		        {
		        	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "WordInput.Log.LineNumber", Long.toString(getLinesInput()))); //$NON-NLS-1$
		        }
			}
		}
		catch(KettleConversionException e) {
			if (getStepMeta().isDoingErrorHandling()) {
				StringBuffer errorDescriptions = new StringBuffer(100);
				StringBuffer errorFields = new StringBuffer(50);
				for (int i=0;i<e.getCauses().size();i++) {
					if (i>0) {
						errorDescriptions.append(", "); //$NON-NLS-1$
						errorFields.append(", "); //$NON-NLS-1$
					}
					errorDescriptions.append(e.getCauses().get(i).getMessage());
					errorFields.append(e.getFields().get(i).toStringMeta());
				}
				
				putError(data.outputRowMeta, e.getRowData(), e.getCauses().size(), errorDescriptions.toString(), errorFields.toString(), "CSVINPUT001"); //$NON-NLS-1$
			} else {
			  // Only forward the first cause.
			  throw new KettleException(e.getMessage(), e.getCauses().get(0));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	private void getFilenamesFromPreviousSteps() throws KettleException {
		List<String> filenames = new ArrayList<String>();
		boolean firstRow = true;
		int index=-1;
		Object[] row = getRow();
		while (row!=null) {
			
			if (firstRow) {
				firstRow=false;
				
				// Get the filename field index...
				//
				String filenameField = environmentSubstitute(meta.getFilenameField());
				index = getInputRowMeta().indexOfValue(filenameField);
				if (index<0) {
					throw new KettleException(BaseMessages.getString(PKG, "WordInput.Exception.FilenameFieldNotFound", filenameField)); //$NON-NLS-1$
				}
			}
				
			String filename = getInputRowMeta().getString(row, index);
			filenames.add(filename);  // add it to the list...
			
			row = getRow(); // Grab another row...
		}
		
		data.filenames = filenames.toArray(new String[filenames.size()]);
		
		logBasic(BaseMessages.getString(PKG, "WordInput.Log.ReadingFromNrFiles", Integer.toString(data.filenames.length))); //$NON-NLS-1$
	}
	
	@Override
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
	  try {
  	  // Close the previous file...
      //
      if (data.fc!=null) {
        data.fc.close();
      }
	  } catch(Exception e) {
	    logError("Error closing file channel", e);
	  }

	  try {
      if (data.fis!=null) {
        data.fis.close();
      }
	  } catch(Exception e) {
	    logError("Error closing file input stream", e);
	  }

    super.dispose(smi, sdi);
	}


		private boolean openNextFile() throws KettleException {
		try {

			// Close the previous file...
			//
//			if (data.fc!=null) {
//				data.fc.close();
//			}

//			if (data.fis!=null) {
//				data.fis.close();
//			}

			if (data.filenr>=data.filenames.length) {
				return false;
			}

			// Open the next one...
			//
			FileObject fileObject = KettleVFS.getFileObject(data.filenames[data.filenr], getTransMeta());

			data.doc = new Document(data.filenames[data.filenr]);
			data.table = (Table) data.doc.getChild(NodeType.TABLE, 0, true);
			data.rowc = data.table.getRows();
			data.rowCound = data.table.getRows().getCount()- new Integer(meta.getStartRowIndex()).intValue();



			// Add filename to result filenames ?
			if(meta.isAddResultFile())
			{
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, fileObject, getTransMeta().getName(), toString());
				resultFile.setComment("File was read by a Csv input step");
				addResultFile(resultFile);
			}

			// Move to the next filename
			//
			data.filenr++;

			// See if we need to skip a row...
			// - If you have a header row checked and if you're not running in parallel
			// - If you're running in parallel, if a header row is checked, if you're at the beginning of a file
			//
//			if (meta.isHeaderPresent()) {
//				if ( (!data.parallel) || // Standard flat file : skip header
//					(data.parallel && data.bytesToSkipInFirstFile<=0)
//					) {
//					readOneRow(false); // skip this row.
//					logBasic(BaseMessages.getString(PKG, "WordInput.Log.HeaderRowSkipped", data.filenames[data.filenr-1])); //$NON-NLS-1$
//				}
//			}

			// Reset the row number pointer...
			//
			data.rowNumber = 1L;

			// Don't skip again in the next file...
//			data.bytesToSkipInFirstFile=-1L;

			return true;
		}
		catch(KettleException e) {
			throw e;
		}
		catch(Exception e) {
			throw new KettleException(e);
		}
	}

	
	/** Read a single row of data from the file... 
	 * 
	 * @param doConversions if you want to do conversions, set to false for the header row.
	 * @return a row of data...
	 * @throws KettleException
	 */
	private Object[] readOneRow(boolean doConversions) throws KettleException {

		try {
			Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());

String s = data.doc.getLists().get(0).toString();
//Object s1 = data.doc.getPageInfo(0);
			if (data.rowIndex < data.table.getRows().getCount()) {
				Row row = data.rowc.get(data.rowIndex);
				CellCollection cellc = row.getCells();
						cellc.forEach(new Consumer<Cell>(){

							int outputIndex=0;
							@Override
							public void accept(Cell tt) {
								// TODO Auto-generated method stub
								try {
									String cellText = tt.toString(SaveFormat.TEXT).trim().replaceAll("\\n", "").replaceAll("\\t", "").replaceAll("\\r", "");
                                    if(doConversions)
									{
										ValueMetaInterface valueMeta = data.outputRowMeta.getValueMeta(outputIndex);
										outputRowData[outputIndex++] = valueMeta.convertBinaryStringToNativeType(cellText.getBytes());
									}
									else
										outputRowData[outputIndex++] = cellText;

									System.out.println(cellText);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						});
				data.rowIndex+=1;
				incrementLinesInput();
				return outputRowData;
			}else
				return null;


		}
		catch (Exception e)
		{
			throw new KettleFileException("Exception reading line using NIO", e);
		}

	}

	/** Read all in one file as one json row
	 *
	 * @return a row of data...
	 * @throws KettleException
	 */
	private Object[] readOneJsonRow() throws KettleException,Exception
	{
		if (data.rowIndex < 1) {
			final Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
			String fileName = meta.getFilename();
			//JSONArray result = new JSONArray();
			//org.json.JSONString result =;
			org.json.JSONObject result =new org.json.JSONObject();
			if(fileName!= null && fileName.endsWith(".docx")){
				// Load the template document.
				File fdoc = new File(fileName);
				FileInputStream fio = new FileInputStream(fdoc);
				@SuppressWarnings("resource")
				XWPFDocument doc = new XWPFDocument(fio);
				List<XWPFTable> tables = doc.getTables();
				for (XWPFTable xwpfTable : tables) {
					JSONArray table_obj = new JSONArray();
					List<XWPFTableRow> rows = xwpfTable.getRows();
					int i = 0;
					for (XWPFTableRow xwpfTableRow : rows) {
						JSONArray row_array = new JSONArray();
						List<XWPFTableCell> cells = xwpfTableRow.getTableCells();
						int j = 0;
						for (XWPFTableCell xwpfTableCell : cells) {
							JSONObject cell_obj = new JSONObject();
							String color = xwpfTableCell.getColor();
							String o = xwpfTableCell.getText().replaceAll("\\n|\\r|\\t", "").trim();

							cell_obj.put("x", i);
							cell_obj.put("y", j);
							cell_obj.put("color", color);
							cell_obj.put("content", o);
							row_array.put(cell_obj);
							j++;
						}
						i++;
						table_obj.put(row_array);
					}
					result.put("table",table_obj);
				}
				outputRowData[0] = result.toString();
			}else if(fileName!= null && fileName.endsWith(".doc")){

				File fdoc = new File("C:/Users/Administrator/Desktop/wordImport/POITest/src/resources/" + fileName);
				FileInputStream fio = new FileInputStream(fdoc);
				HWPFDocument doc = new HWPFDocument(fio);
				Range  range  = doc.getRange();
				TableIterator it = new TableIterator(range);
				int tt = 0;
				while(it.hasNext()){
					org.apache.poi.hwpf.usermodel.Table table = it.next();
					for (int i = 0; i < table.numRows(); i++) {
						TableRow row = table.getRow(i);
						for (int j = 0; j < row.numCells(); j++) {
							TableCell cell = row.getCell(j);
							String o = cell.text().replaceAll("\\n|\\r|\\t", "").trim();
						}
					}

				}
			}
			data.rowIndex+=1;
			incrementLinesInput();
			return outputRowData;
		}else
			return null;
	}

  public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(WordInputMeta)smi;
		data=(WordInputData)sdi;
		
		if (super.init(smi, sdi)) {
//			data.preferredBufferSize = Integer.parseInt(environmentSubstitute(meta.getTableNr()));
			
			// If the step doesn't have any previous steps, we just get the filename.
			// Otherwise, we'll grab the list of filenames later...
			//
			if (getTransMeta().findNrPrevSteps(getStepMeta())==0) {
				String filename = environmentSubstitute(meta.getFilename());

				if (Const.isEmpty(filename)) {
					logError(BaseMessages.getString(PKG, "WordInput.MissingFilename.Message")); //$NON-NLS-1$
					return false;
				}

				data.filenames = new String[] { filename, };
			}
			else {
				data.filenames = null;
				data.filenr = 0;
			}
			data.rowIndex=new Integer(meta.getStartRowIndex()).intValue();

			data.isAddingRowNumber = !Const.isEmpty(meta.getRowNumField());

			return true;

		}
		return false;
	}
	
	public void closeFile() throws KettleException {
		
		try {
			if (data.fc!=null) {
				data.fc.close();
			}
			if (data.fis!=null) {
				data.fis.close();
			}
		} catch (IOException e) {
			throw new KettleException("Unable to close file channel for file '"+data.filenames[data.filenr-1],e);
		}
	}
	
	/**
	 * This method is borrowed from TextFileInput
	 * 
	 * @param log
	 * @param line
	 * @param delimiter
	 * @param enclosure
	 * @param escapeCharacter
	 * @return
	 * @throws KettleException
	 */
	public static final String[] guessStringsFromLine(LogChannelInterface log, String line, String delimiter, String enclosure, String escapeCharacter) throws KettleException
  {
    List<String> strings = new ArrayList<String>();
        int fieldnr;
        
    String pol; // piece of line

    try
    {
      if (line == null) return null;

      // Split string in pieces, only for CSV!

      fieldnr = 0;
      int pos = 0;
      int length = line.length();
      boolean dencl = false;

              int len_encl = (enclosure == null ? 0 : enclosure.length());
              int len_esc = (escapeCharacter == null ? 0 : escapeCharacter.length());

      while (pos < length)
      {
        int from = pos;
        int next;

        boolean encl_found;
        boolean contains_escaped_enclosures = false;
        boolean contains_escaped_separators = false;

        // Is the field beginning with an enclosure?
        // "aa;aa";123;"aaa-aaa";000;...
        if (len_encl > 0 && line.substring(from, from + len_encl).equalsIgnoreCase(enclosure))
        {
                      if (log.isRowLevel()) log.logRowlevel(BaseMessages.getString(PKG, "WordInput.Log.ConvertLineToRowTitle"), BaseMessages.getString(PKG, "WordInput.Log.ConvertLineToRow",line.substring(from, from + len_encl))); //$NON-NLS-1$ //$NON-NLS-2$
          encl_found = true;
          int p = from + len_encl;

          boolean is_enclosure = len_encl > 0 && p + len_encl < length
              && line.substring(p, p + len_encl).equalsIgnoreCase(enclosure);
          boolean is_escape = len_esc > 0 && p + len_esc < length
              && line.substring(p, p + len_esc).equalsIgnoreCase(escapeCharacter);

          boolean enclosure_after = false;
          
          // Is it really an enclosure? See if it's not repeated twice or escaped!
          if ((is_enclosure || is_escape) && p < length - 1) 
          {
            String strnext = line.substring(p + len_encl, p + 2 * len_encl);
            if (strnext.equalsIgnoreCase(enclosure))
            {
              p++;
              enclosure_after = true;
              dencl = true;

              // Remember to replace them later on!
              if (is_escape) contains_escaped_enclosures = true; 
            }
          }

          // Look for a closing enclosure!
          while ((!is_enclosure || enclosure_after) && p < line.length())
          {
            p++;
            enclosure_after = false;
            is_enclosure = len_encl > 0 && p + len_encl < length && line.substring(p, p + len_encl).equals(enclosure);
            is_escape = len_esc > 0 && p + len_esc < length && line.substring(p, p + len_esc).equals(escapeCharacter);

            // Is it really an enclosure? See if it's not repeated twice or escaped!
            if ((is_enclosure || is_escape) && p < length - 1) // Is
            {
              String strnext = line.substring(p + len_encl, p + 2 * len_encl);
              if (strnext.equals(enclosure))
              {
                p++;
                enclosure_after = true;
                dencl = true;

                // Remember to replace them later on!
                if (is_escape) contains_escaped_enclosures = true; // remember
              }
            }
          }

          if (p >= length) next = p;
          else next = p + len_encl;

                      if (log.isRowLevel()) log.logRowlevel(BaseMessages.getString(PKG, "WordInput.Log.ConvertLineToRowTitle"), BaseMessages.getString(PKG, "WordInput.Log.EndOfEnclosure", ""+ p)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        else
        {
          encl_found = false;
          boolean found = false;
          int startpoint = from;
          int tries = 1;
          do
          {
            next = line.indexOf(delimiter, startpoint); 

            // See if this position is preceded by an escape character.
            if (len_esc > 0 && next - len_esc > 0)
            {
              String before = line.substring(next - len_esc, next);

              if (escapeCharacter != null && escapeCharacter.equals(before))
              {
                // take the next separator, this one is escaped...
                startpoint = next + 1; 
                tries++;
                contains_escaped_separators = true;
              }
              else
              {
                found = true;
              }
            }
            else
            {
              found = true;
            }
          }
          while (!found && next >= 0);
        }
        if (next == -1) next = length;

        if (encl_found)
        {
          pol = line.substring(from + len_encl, next - len_encl);
                      if (log.isRowLevel()) log.logRowlevel(BaseMessages.getString(PKG, "WordInput.Log.ConvertLineToRowTitle"), BaseMessages.getString(PKG, "WordInput.Log.EnclosureFieldFound", ""+ pol)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        else
        {
          pol = line.substring(from, next);
                      if (log.isRowLevel()) log.logRowlevel(BaseMessages.getString(PKG, "WordInput.Log.ConvertLineToRowTitle"), BaseMessages.getString(PKG, "WordInput.Log.NormalFieldFound",""+ pol)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        if (dencl)
        {
          StringBuilder sbpol = new StringBuilder(pol);
          int idx = sbpol.indexOf(enclosure + enclosure);
          while (idx >= 0)
          {
            sbpol.delete(idx, idx + (enclosure == null ? 0 : enclosure.length()) );
            idx = sbpol.indexOf(enclosure + enclosure);
          }
          pol = sbpol.toString();
        }

        //  replace the escaped enclosures with enclosures... 
        if (contains_escaped_enclosures) 
        {
          String replace = escapeCharacter + enclosure;
          String replaceWith = enclosure;

          pol = Const.replace(pol, replace, replaceWith);
        }

        //replace the escaped separators with separators...
        if (contains_escaped_separators) 
        {
          String replace = escapeCharacter + delimiter;
          String replaceWith = delimiter;
          
          pol = Const.replace(pol, replace, replaceWith);
        }

        // Now add pol to the strings found!
        strings.add(pol);

        pos = next + delimiter.length();
        fieldnr++;
      }
      if ( pos == length )
      {
        if (log.isRowLevel()) log.logRowlevel(BaseMessages.getString(PKG, "WordInput.Log.ConvertLineToRowTitle"), BaseMessages.getString(PKG, "WordInput.Log.EndOfEmptyLineFound")); //$NON-NLS-1$ //$NON-NLS-2$
        strings.add(""); //$NON-NLS-1$
                  fieldnr++;
      }
    }
    catch (Exception e)
    {
      throw new KettleException(BaseMessages.getString(PKG, "WordInput.Log.Error.ErrorConvertingLine",e.toString()), e); //$NON-NLS-1$
    }

    return strings.toArray(new String[strings.size()]);
  }

  public boolean isWaitingForData() {
	return true;
  }

	public static boolean getLicense() {
		boolean result = false;
		try {
			//InputStream is = WordInput.class.getClassLoader().getResourceAsStream("aspose/license.xml");
			InputStream is = new FileInputStream(new File("./aspose/license.xml"));
			License aposeLic = new License();
			aposeLic.setLicense(is);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}