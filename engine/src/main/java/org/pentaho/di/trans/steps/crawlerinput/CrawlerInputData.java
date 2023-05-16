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


 package org.pentaho.di.trans.steps.crawlerinput;

 import org.pentaho.di.core.row.RowMetaInterface;
 import org.pentaho.di.trans.step.BaseStepData;
 import org.pentaho.di.trans.step.StepDataInterface;

 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Queue;


 /**
  * web site info extractor
  *
  * @author Jason
  * @since 10-Aug-2010
  */
 public class CrawlerInputData extends BaseStepData implements StepDataInterface
 {

	 public int rowCount;
	 public HashSet<String> allListPage=  new HashSet();
	 public Queue<String> listPageQueue = new LinkedList();

     public Queue<Object[]> contentRowQueue = new LinkedList<>();

     public int id=0;
     public RowMetaInterface outputRowMeta;
     public int rowsWritten;
     public int rowLimit=10000;
     //public Object[] outputRowData;

     /**
	  *
	  */
	 public CrawlerInputData()
	 {
		 super();
	 }

 }