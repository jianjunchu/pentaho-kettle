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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.csvinput.CrLfMatcherInterface;
import org.pentaho.di.trans.steps.csvinput.PatternMatcherInterface;
import org.pentaho.di.trans.steps.textfileinput.EncodingType;
import com.aspose.words.Document;
import com.aspose.words.Table;
import com.aspose.words.RowCollection;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class WordInputData extends BaseStepData implements StepDataInterface
{
	public FileChannel fc;
//	public ByteBuffer bb;
	public RowMetaInterface convertRowMeta;
	public RowMetaInterface outputRowMeta;
	
//	public byte[] byteBuffer;
//	public int    startBuffer;
//	public int    endBuffer;
	public int    tableNr;

	public byte[] delimiter;
//	public byte[] enclosure;
	
//	public int preferredBufferSize;
	public String[] filenames;
	public int      filenr;
	public int      startFilenr;
	public byte[]   binaryFilename;
	public long fileSize;
	public FileInputStream fis;
	
	public boolean  isAddingRowNumber;
	public long     rowNumber;
//	public boolean stopReading;
	public int stepNumber;
//	public int totalNumberOfSteps;
//	public List<Long> fileSizes;
//	public long totalFileSize;
//	public long blockToRead;
//	public long startPosition;
//	public long endPosition;
//	public long bytesToSkipInFirstFile;
			
//	public long totalBytesRead;
	
//	public boolean parallel;
	public int filenameFieldIndex;
	public int rownumFieldIndex;
//    public EncodingType encodingType;
//    public PatternMatcherInterface delimiterMatcher;
//    public PatternMatcherInterface enclosureMatcher;
//    public CrLfMatcherInterface crLfMatcher;
	public Document doc;
	public Table table;
	public int rowIndex=0;
	public RowCollection rowc;
	public int rowCound;

	/**
	 * 
	 */
	public WordInputData()
	{
		super();
//		byteBuffer = new byte[] {};
//		startBuffer = 0;
//		endBuffer = 0;
//		totalBytesRead = 0;
	}

	/**
      <pre>	 
      [abcd "" defg] --> [abcd " defg]
      [""""] --> [""]
      [""] --> ["]
      </pre>	 

     @return the byte array with escaped enclosures escaped.
	*/
//	public byte[] removeEscapedEnclosures(byte[] field, int nrEnclosuresFound) {
//		byte[] result = new byte[field.length-nrEnclosuresFound];
//		int resultIndex=0;
//		for (int i=0;i<field.length;i++)
//		{
//			if (field[i]==enclosure[0])
//			{
//				if (i+1<field.length && field[i+1]==enclosure[0])
//				{
//					// field[i]+field[i+1] is an escaped enclosure...
//					// so we ignore this one
//					// field[i+1] will be picked up on the next iteration.
//				}
//				else
//				{
//					// Not an escaped enclosure...
//					result[resultIndex++] = field[i];
//				}
//			}
//			else
//			{
//				result[resultIndex++] = field[i];
//			}
//		}
//		return result;
//	}



//	  int getStartBuffer() {
//	    return startBuffer;
//	  }
//
//	  void setStartBuffer( int startBuffer ) {
//	    this.startBuffer = startBuffer;
//	  }
//
//	  int getEndBuffer() {
//	    return endBuffer;
//	  }

//	  boolean newLineFound() {
//	    return crLfMatcher.isReturn( byteBuffer, endBuffer ) || crLfMatcher.isLineFeed( byteBuffer, endBuffer );
//	  }
//
//	  boolean delimiterFound() {
//	    return delimiterMatcher.matchesPattern( byteBuffer, endBuffer, delimiter );
//	  }
//
//	  boolean enclosureFound() {
//	    return enclosureMatcher.matchesPattern( byteBuffer, endBuffer, enclosure );
//	  }

}
