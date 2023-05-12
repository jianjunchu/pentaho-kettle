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

 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;

 import org.pentaho.di.core.exception.KettleException;
 import org.pentaho.di.core.row.RowMeta;
 import org.pentaho.di.core.row.RowMetaInterface;
 import org.pentaho.di.core.row.ValueMeta;
 import org.pentaho.di.core.row.ValueMetaInterface;
 import org.pentaho.di.trans.Trans;
 import org.pentaho.di.trans.TransMeta;
 import org.pentaho.di.trans.step.BaseStep;
 import org.pentaho.di.trans.step.StepDataInterface;
 import org.pentaho.di.trans.step.StepInterface;
 import org.pentaho.di.trans.step.StepMeta;
 import org.pentaho.di.trans.step.StepMetaInterface;
 import org.pentaho.di.i18n.BaseMessages;

 /**
  * web info extractor
  *
  * @author Jason
  * @since 10-Aug-2010
  */
 public class CrawlerInput extends BaseStep implements StepInterface
 {
	 private static Class<?> PKG = CrawlerInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	 private CrawlerInputMeta meta;
	 private CrawlerInputData data;


	 public CrawlerInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	 {
		 super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	 }

	 public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	 {
		 meta=(CrawlerInputMeta)smi;
		 data=(CrawlerInputData)sdi;
		 boolean retval=true;
		 if(first)
		 {
			 first = false;
			 String startURL = environmentSubstitute(meta.getStartPageURL());
			 try {
//				 HTMLTractor extractor = new HTMLTractor(startURL);
//				 String startPageContent = extractor.getSource("utf-8");
//				 extractListPage(startPageContent);
				 ArrayList<Object[]> RowsInStartPage = extractContentPages(startURL);
				 for (int i=0 ; i<RowsInStartPage.size();i++)
				 {
					 Object[] r = RowsInStartPage.get(i);
					 data.contentRowQueue.add(r);
				 }
				 if(data.listPageQueue.size()>0)
				 {
					 String listPageURL =data.listPageQueue.poll();
					 ArrayList<Object[]> rows = extractContentPages(listPageURL);
					 for (int i=0 ; i<rows.size();i++)
					 {
						 Object[] r = rows.get(i);
						 data.contentRowQueue.add(r);
					 }
				 }
				 //process(data.listPageQueue);
			 }catch (Exception e)
			 {
				 e.printStackTrace();
			 }
			 if (checkFeedback(getLinesRead()))
			 {
				 if(log.isBasic()) logBasic(BaseMessages.getString("CrawlerInput.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
			 }
		 }
		 else if (data.listPageQueue.size()>0 && data.contentRowQueue.size()<meta.getContentQueueBufferSize()) //continue to extract
		 {
			 try {
				 String listPageURL =data.listPageQueue.poll();
				 ArrayList<Object[]> rows = extractContentPages(listPageURL);//extract content pages in the list page and extract other list pages
				 for (int i=0 ; i<rows.size();i++)
				 {
					 Object[] r = rows.get(i);
					 data.contentRowQueue.add(r);
				 }
			 }catch (Exception e)
			 {
				 e.printStackTrace();
			 }

			 if (checkFeedback(getLinesRead()))
			 {
				 if(log.isBasic()) logBasic(BaseMessages.getString("CrawlerInput.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
			 }
		 }

		 if(data.rowLimit<0 || data.rowsWritten<data.rowLimit) {
			 if(data.contentRowQueue.size()>0) {
				 Object[] r = data.contentRowQueue.poll();
				 putRow(data.outputRowMeta, r);
				 data.rowsWritten++;
				 if (log.isRowLevel()) {
					 logRowlevel(BaseMessages.getString(PKG, "CrawlerInput.Log.LineNumber", Long.toString(data.rowsWritten), data.outputRowMeta.getString(r)));
				 }else if(log.isBasic())
				 {
					 logBasic(r[1].toString());
				 }
				 if (checkFeedback(data.rowsWritten)) {
					 if (log.isBasic())
						 logBasic(BaseMessages.getString(PKG, "CrawlerInput.Log.LineNumber", Long.toString(data.rowsWritten)));
				 }
			 }else {
				 setOutputDone();  // signal end to receiver(s)
				 return false;
			 }
		 }else{
			 setOutputDone();  // signal end to receiver(s)
			 return false;
		 }

		 return retval;
	 }

	 /**
	  * extract Content Pages to results and extract list Page urls to queue
	  * @param   fromPageUrl
	  * @return
	  */
	 private ArrayList<Object[]> extractContentPages(String fromPageUrl) {
		 ArrayList list = new ArrayList();
		 try {
			 HTMLTractor listPageTractor = new HTMLTractor(fromPageUrl);
			 String pageContent = listPageTractor.getSource("utf-8");
			 extractListPage(pageContent,fromPageUrl);//extract list page from html
			 String contentPageUrl =environmentSubstitute(meta.getContentPageURLPattern());
			 list = extractContentPageImpl(fromPageUrl,contentPageUrl,pageContent);
			 if(list.size()==0&&contentPageUrl.indexOf("http")>-1)
			 {
				 contentPageUrl = contentPageUrl.substring(contentPageUrl.indexOf("/",contentPageUrl.indexOf("//")+2));
				 list = extractContentPageImpl(fromPageUrl,contentPageUrl,pageContent);
			 }

		 } catch (Exception e) {
			 throw new RuntimeException(e);
		 }
		 return list;
	 }

	 private ArrayList extractContentPageImpl(String fromPageUrl, String contentPageUrl,String pageContent) {
		 ArrayList list = new ArrayList();
		 HashSet extractedUrl = new HashSet();
		 Pattern contentPagePattern = Pattern.compile(contentPageUrl);
		 Matcher contentPageMatcher = contentPagePattern.matcher(pageContent);
		 int count=0;
		 try {
			 while (contentPageMatcher.find()) {
				 count++;
				 String contentURL = contentPageMatcher.group();
				 String url;
				 if (contentURL.startsWith("http")) {
					 url = contentURL;
				 } else if (contentURL.startsWith("/")) //matched absolut path
				 {
					 url = getDomainName(fromPageUrl) + contentURL; //fromPageUrl should start with http:// or https://
				 } else {
					 url = fromPageUrl.substring(0, fromPageUrl.lastIndexOf("/") + 1) + contentURL;
				 }
				 if(!extractedUrl.contains(url)) {
					 HTMLTractor tractor = new HTMLTractor(url);
					 String content = tractor.getSource("utf-8");//extract content page content
					 Object[] row = new Object[4];
					 row[0] = url;//url
					 row[1] = contentURL.substring(contentURL.lastIndexOf("/") + 1, contentURL.length());//file name
					 row[2] = content;//content
					 list.add(row);
					 extractedUrl.add(url);//url saved to already extracted set
				 }
			 }
		 }catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 return list;
	 }

	 /**
	  *
	  * @param pageContent
	  * @param fromPageUrl
	  */
	 private void extractListPage(String pageContent,String fromPageUrl) {
		 String listPageURLPattern = environmentSubstitute(meta.getListPageURLPattern());
		 int count = extractListPageImpl( pageContent, fromPageUrl, listPageURLPattern);
		 if(count==0 && listPageURLPattern.startsWith("http"))//if not found, remove http in list page pattern, and try again
		 {
			 listPageURLPattern = listPageURLPattern.substring(listPageURLPattern.indexOf("/",listPageURLPattern.indexOf("//")+2));
			 extractListPageImpl( pageContent, fromPageUrl, listPageURLPattern);
		 }
	 }

	 private int extractListPageImpl(String pageContent, String fromPageUrl, String listPageURLPattern) {
		 Pattern pattern = Pattern.compile(listPageURLPattern);
		 Matcher matcher = pattern.matcher(pageContent);
		 int count=0;
		 while (matcher.find()) {
			 count++;
			 String matched = matcher.group();
			 String url = null;
			 if(matched.startsWith("http") )
			 {
				 url= matched;
			 }
			 else if(matched.startsWith("/") ) //matched absolut path
			 {
				 url=  getDomainName(fromPageUrl)+matched; //fromPageUrl should start with http:// or https://
			 }else
			 {
				 url= fromPageUrl.substring(0,fromPageUrl.lastIndexOf("/")+1)+matched;
			 }
			 if(!data.allListPage.contains(url)) {
				 data.allListPage.add(url);
				 data.listPageQueue.add(url);
			 }
		 }
		 return count;
	 }

	 public String getDomainName(String url)
	 {
		 int index = url.indexOf("//");
		 if(index ==-1)
			return null;
		 return url.substring(0,url.indexOf("/",index+3));
	 }

	 public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	 {
		 meta=(CrawlerInputMeta)smi;
		 data=(CrawlerInputData)sdi;

		 if (super.init(smi, sdi))
		 {
			 data.rowLimit = meta.getRowLimit();
			 data.rowsWritten = 0;

			 RowMetaInterface outputRowMeta = new RowMeta();
			 int stringType = ValueMeta.getType("String");
			 int intType = ValueMeta.getType("Integer");
			 ValueMetaInterface urlMeta = new ValueMeta( "url", stringType);
			 urlMeta.setLength(-1);
			 ValueMetaInterface fileNameMeta = new ValueMeta( "file_name", stringType);
			 fileNameMeta.setLength(-1);
			 ValueMetaInterface contentMeta = new ValueMeta( "content", stringType);
			 contentMeta.setLength(-1);
			 outputRowMeta.addValueMeta(urlMeta);
			 outputRowMeta.addValueMeta(fileNameMeta);
			 outputRowMeta.addValueMeta(contentMeta);
			 data.outputRowMeta =outputRowMeta;
			 return true;
		 }
		 return false;
	 }

	 public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	 {
		 super.dispose(smi, sdi);
	 }




 }