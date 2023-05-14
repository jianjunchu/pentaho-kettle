/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the GNU Lesser General Public License, Version 2.1. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho
 * Data Integration.  The Initial Developer is Samatarn.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

/*
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.ui.trans.steps.crawlerinput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.trans.steps.crawlerinput.CrawlerInputMeta;
import org.pentaho.di.i18n.BaseMessages;

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

public class CrawlerInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = CrawlerInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CrawlerInputMeta input;
	private TextVar wStartPageURL;

	private Group wListPageGroup;

	protected Label wlListPageURL;
	protected Text wListPageURL;
	protected  Button wListPageButton;
	private TextVar wListPageURLPattern;


	private Group wContentPageGroup;
	protected Label wlContentPageURL;
	protected Text wContentPageURL;
	protected  Button wContentPageButton;
	private TextVar wContentPageURLPattern;

	private TextVar wRowLimit;
	private TextVar wQueueSize;
	public CrawlerInputDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(CrawlerInputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "CrawlerInputDialog.Shell.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "CrawlerInputDialog.Stepname.Label")); //$NON-NLS-1$
		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// Start Page URL
		Label wlStartPageURL = new Label(shell, SWT.RIGHT);
		wlStartPageURL.setText("* "+BaseMessages.getString(PKG, "CrawlerInputDialog.StartPage.Label")); //$NON-NLS-1$
		wlStartPageURL.setToolTipText(BaseMessages.getString(PKG, "CrawlerInputDialog.StartPage.Label.ToolTip"));
		props.setLook(wlStartPageURL);
		FormData fdlStartPageURL = new FormData();
		fdlStartPageURL.left = new FormAttachment(0, 0);
		fdlStartPageURL.right= new FormAttachment(middle, -margin);
		fdlStartPageURL.top  = new FormAttachment(wStepname, margin);
		wlStartPageURL.setLayoutData(fdlStartPageURL);
		wStartPageURL =new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStartPageURL.setText(stepname);
		props.setLook(wStartPageURL);
		wStartPageURL.addModifyListener(lsMod);
		FormData fdStartPageURL = new FormData();
		fdStartPageURL.left = new FormAttachment(middle, 0);
		fdStartPageURL.top  = new FormAttachment(wStepname, margin);
		fdStartPageURL.right= new FormAttachment(100, 0);
		wStartPageURL.setLayoutData(fdStartPageURL);

		wListPageGroup = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wListPageGroup);
		wListPageGroup.setText(BaseMessages.getString(PKG, "CrawlerInputDialog.ListPageGroup.Label"));
		FormLayout groupLayout = new FormLayout();
		groupLayout.marginWidth = 10;
		groupLayout.marginHeight = 10;
		wListPageGroup.setLayout(groupLayout);
		FormData fdListPageGroup = new FormData();
		fdListPageGroup.left = new FormAttachment(0, margin);
		fdListPageGroup.top = new FormAttachment(wStartPageURL, margin);
		fdListPageGroup.right = new FormAttachment(100, -margin);
		wListPageGroup.setLayoutData(fdListPageGroup);

		//ListPageURL
		Label wlListPageURL = new Label(wListPageGroup, SWT.RIGHT);
		wlListPageURL.setText(BaseMessages.getString(PKG, "CrawlerInputDialog.ListPageURL.Label")); //$NON-NLS-1$
		wlListPageURL.setToolTipText(BaseMessages.getString(PKG, "CrawlerInputDialog.ListPageURL.Label.ToolTip"));
		props.setLook(wlListPageURL);
		FormData fdlListPageURL = new FormData();
		fdlListPageURL.left = new FormAttachment(0, 0);
		fdlListPageURL.right= new FormAttachment(middle, -margin);
		fdlListPageURL.top  = new FormAttachment(wStartPageURL, margin);
		wlListPageURL.setLayoutData(fdlListPageURL);
		wListPageURL =new Text(wListPageGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wListPageURL.setText("");
		wListPageURL.setToolTipText(BaseMessages.getString(PKG, "CrawlerInputDialog.ListPageURL.Label.ToolTip"));
		props.setLook(wListPageURL);
		wListPageURL.addModifyListener(lsMod);
		FormData fdListPageURL = new FormData();
		fdListPageURL.left = new FormAttachment(middle, 0);
		fdListPageURL.top  = new FormAttachment(wStartPageURL, margin);
		fdListPageURL.right= new FormAttachment(80, 0);
		wListPageURL.setLayoutData(fdListPageURL);

		wListPageButton=new Button(wListPageGroup, SWT.NONE );
		wListPageButton.setText(BaseMessages.getString(PKG, "CrawlerInputDialog.Button.GenerateRegExp"));
		props.setLook(wListPageButton);
		FormData fdPageButton=new FormData();
		fdPageButton.left = new FormAttachment(wListPageURL, 0);
		fdPageButton.top  = new FormAttachment(wStartPageURL, 0);
		fdPageButton.right= new FormAttachment(100, 0);

		wListPageButton.setLayoutData(fdPageButton);
		wListPageButton.addSelectionListener(new SelectionAdapter()
											 {
												 public void widgetSelected(SelectionEvent e)
												 {
													 String str = wListPageURL.getText();
													 try {
														 wListPageURLPattern.setText(getPatternStr(str));
													 } catch (Exception ex) {
														 new ErrorDialog(shell, BaseMessages.getString(PKG, "CrawlerInputDialog.ErrorDialog.UnableToGeneratePattern.Title"), BaseMessages.getString(PKG, "CrawlerInputDialog.ErrorDialog.UnableToGeneratePattern.Message"), ex);
													 }
												 }
											 }
		);

		// ListPageURLPattern
		Label wlListPageURLPattern = new Label(wListPageGroup, SWT.RIGHT);
		wlListPageURLPattern.setText("* "+BaseMessages.getString(PKG, "CrawlerInputDialog.ListPageURLPattern.Label")); //$NON-NLS-1$
		props.setLook(wlListPageURLPattern);
		FormData fdlListPageURLPattern = new FormData();
		fdlListPageURLPattern.left = new FormAttachment(0, 0);
		fdlListPageURLPattern.right= new FormAttachment(middle, -margin);
		fdlListPageURLPattern.top  = new FormAttachment(wListPageURL, margin);
		wlListPageURLPattern.setLayoutData(fdlListPageURLPattern);
		wListPageURLPattern =new TextVar(transMeta, wListPageGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wListPageURLPattern.setText("");
		props.setLook(wListPageURLPattern);
		wListPageURLPattern.addModifyListener(lsMod);
		FormData fdListPageURLPattern = new FormData();
		fdListPageURLPattern.left = new FormAttachment(middle, 0);
		fdListPageURLPattern.top  = new FormAttachment(wListPageURL, margin);
		fdListPageURLPattern.right= new FormAttachment(100, 0);
		wListPageURLPattern.setLayoutData(fdListPageURLPattern);


		wContentPageGroup = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wContentPageGroup);
		wContentPageGroup.setText(BaseMessages.getString(PKG, "CrawlerInputDialog.ContentPageGroup.Label"));
		FormLayout groupLayout2 = new FormLayout();
		groupLayout2.marginWidth = 10;
		groupLayout2.marginHeight = 10;
		wContentPageGroup.setLayout(groupLayout2);
		FormData fdContentPageGroup = new FormData();
		fdContentPageGroup.left = new FormAttachment(0, margin);
		fdContentPageGroup.top = new FormAttachment(wListPageGroup, margin);
		fdContentPageGroup.right = new FormAttachment(100, -margin);
		wContentPageGroup.setLayoutData(fdContentPageGroup);

		//ContentPageURL
		Label wlContentPageURL = new Label(wContentPageGroup, SWT.RIGHT);
		wlContentPageURL.setText(BaseMessages.getString(PKG, "CrawlerInputDialog.ContentPageURL.Label")); //$NON-NLS-1$
		wlContentPageURL.setToolTipText(BaseMessages.getString(PKG, "CrawlerInputDialog.ContentPageURL.Label.ToolTip"));
		props.setLook(wlContentPageURL);
		FormData fdlContentPageURL = new FormData();
		fdlContentPageURL.left = new FormAttachment(0, 0);
		fdlContentPageURL.right= new FormAttachment(middle, -margin);
		fdlContentPageURL.top  = new FormAttachment(wListPageURLPattern, margin);
		wlContentPageURL.setLayoutData(fdlContentPageURL);
		wContentPageURL =new Text(wContentPageGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wContentPageURL.setText("");
		wContentPageURL.setToolTipText(BaseMessages.getString(PKG, "CrawlerInputDialog.ContentPageURL.Label.ToolTip"));
		props.setLook(wContentPageURL);
		wContentPageURL.addModifyListener(lsMod);
		FormData fdContentPageURL = new FormData();
		fdContentPageURL.left = new FormAttachment(middle, 0);
		fdContentPageURL.top  = new FormAttachment(wListPageGroup, margin);
		fdContentPageURL.right= new FormAttachment(80, 0);
		wContentPageURL.setLayoutData(fdContentPageURL);

		wContentPageButton=new Button(wContentPageGroup, SWT.NONE );
		wContentPageButton.setText(BaseMessages.getString(PKG, "CrawlerInputDialog.Button.GenerateRegExp"));
		wContentPageButton.setToolTipText(BaseMessages.getString(PKG, "CrawlerInputDialog.Button.GenerateRegExp.ToolTip"));
		props.setLook(wContentPageButton);
		FormData fdContentPageButton=new FormData();
		fdContentPageButton.left = new FormAttachment(wContentPageURL, 0);
		fdContentPageButton.top  = new FormAttachment(wListPageGroup, 0);
		fdContentPageButton.right= new FormAttachment(100, 0);

		wContentPageButton.setLayoutData(fdContentPageButton);
		wContentPageButton.addSelectionListener(new SelectionAdapter()
												{
													public void widgetSelected(SelectionEvent e)
													{
														String str = wContentPageURL.getText();
														try {
															wContentPageURLPattern.setText(getPatternStr(str));
														} catch (Exception ex) {
															new ErrorDialog(shell, BaseMessages.getString(PKG, "CrawlerInputDialog.ErrorDialog.UnableToGeneratePattern.Title"), BaseMessages.getString(PKG, "CrawlerInputDialog.ErrorDialog.UnableToGeneratePattern.Message"), ex);
														}
													}
												}
		);



//		// Content Page URL Pattern
		Label wlContentPageURLPattern = new Label(wContentPageGroup, SWT.RIGHT);
		wlContentPageURLPattern.setText("* "+BaseMessages.getString(PKG, "CrawlerInputDialog.ContentPageURLPattern.Label")); //$NON-NLS-1$
		props.setLook(wlContentPageURLPattern);
		FormData fdlContentPageURLPattern = new FormData();
		fdlContentPageURLPattern.left = new FormAttachment(0, 0);
		fdlContentPageURLPattern.right= new FormAttachment(middle, -margin);
		fdlContentPageURLPattern.top  = new FormAttachment(wContentPageURL, margin);
		wlContentPageURLPattern.setLayoutData(fdlContentPageURLPattern);
		wContentPageURLPattern =new TextVar(transMeta, wContentPageGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wContentPageURLPattern.setText(stepname);
		props.setLook(wContentPageURLPattern);
		wContentPageURLPattern.addModifyListener(lsMod);
		FormData fdContentPageURLPattern = new FormData();
		fdContentPageURLPattern.left = new FormAttachment(middle, 0);
		fdContentPageURLPattern.top  = new FormAttachment(wContentPageURL, margin);
		fdContentPageURLPattern.right= new FormAttachment(100, 0);
		wContentPageURLPattern.setLayoutData(fdContentPageURLPattern);
//


		// row limit
		Label wlRowLimit = new Label(shell, SWT.RIGHT);
		props.setLook(wlRowLimit);
		wlRowLimit.setText("* "+BaseMessages.getString(PKG, "CrawlerInputDialog.Rowlimit.Label"));
		FormData fdlRowLimit = new FormData();
		fdlRowLimit.top   = new FormAttachment(wContentPageGroup, margin);
		fdlRowLimit.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlRowLimit.right = new FormAttachment(middle, 0);
		wlRowLimit.setLayoutData(fdlRowLimit);
		wRowLimit = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wRowLimit);
		FormData fdRowLimit = new FormData();
		fdRowLimit.top  = new FormAttachment(wContentPageGroup, margin);
		fdRowLimit.left = new FormAttachment(middle, margin); // To the right of the label
		fdRowLimit.right= new FormAttachment(95, 0);
		wRowLimit.setLayoutData(fdRowLimit);

		// row limit
		Label wlQueueSize = new Label(shell, SWT.RIGHT);
		props.setLook(wlQueueSize);
		wlQueueSize.setText(BaseMessages.getString(PKG, "CrawlerInputDialog.QueueSize.Label"));
		FormData fdlQueueSize = new FormData();
		fdlQueueSize.top   = new FormAttachment(wRowLimit, margin);
		fdlQueueSize.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlQueueSize.right = new FormAttachment(middle, 0);
		wlQueueSize.setLayoutData(fdlQueueSize);
		wQueueSize = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wQueueSize);
		FormData fdQueueSize = new FormData();
		fdQueueSize.top  = new FormAttachment(wRowLimit, margin);
		fdQueueSize.left = new FormAttachment(middle, margin); // To the right of the label
		fdQueueSize.right= new FormAttachment(95, 0);
		wQueueSize.setLayoutData(fdQueueSize);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wQueueSize);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wStepname.addSelectionListener( lsDef );
		wContentPageURLPattern.addSelectionListener( lsDef );
		wStartPageURL.addSelectionListener( lsDef );
		wListPageURLPattern.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();

		getData();
		input.setChanged(changed);
		checkPriviledges();
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	private String getPatternStr(String str) throws Exception {
		String result="";
		str = str.toLowerCase();
		if(!str.startsWith("http://") && !str.startsWith("https://"))
			throw new Exception("网址应该以http或https开头");
		int domainStartIndex = str.indexOf("//")+2;
		int domainEndIndex = str.indexOf("/",domainStartIndex);
		if(domainEndIndex==-1)
		{
			throw new Exception("不是页面 URL");
		}
		int fileNameStartIndex= str.lastIndexOf("/",domainEndIndex+1);
		String fileName = str.substring(fileNameStartIndex);
		return str.substring(0,fileNameStartIndex)+replaceDigitalWithPattern(fileName);
	}
	public String replaceDigitalWithPattern(String str)
	{
		String result="";
		char[] chars = str.toCharArray();
		boolean firstDig=true;
		for (int i = 0; i < chars.length; i++) {
			if( 48 <= chars[i] && chars[i]<= 57 ) {
				if (firstDig) {
					result+="\\d+";
					firstDig=false;
				}
				continue;
			}
			else {
				result += chars[i];
				firstDig = true;
			}
		}
		return result;
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		wContentPageURLPattern.setText(Const.NVL(input.getContentPageURLPattern(), ""));
		wStartPageURL.setText(Const.NVL(input.getStartPageURL(), ""));
		wListPageURLPattern.setText(Const.NVL(input.getListPageURLPattern(), ""));
		wRowLimit.setText(new Integer(input.getRowLimit()).toString());
		wQueueSize.setText(new Integer(input.getContentQueueBufferSize()).toString());
		wStepname.selectAll();
	}

	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}

	private void ok()
	{
		input.setContentPageURLPattern(wContentPageURLPattern.getText());
		input.setStartPageURL(wStartPageURL.getText());
		input.setListPageURLPattern(wListPageURLPattern.getText());
		input.setRowLimit(Const.toInt(wRowLimit.getText(), -1));
		input.setContentQueueBufferSize(Const.toInt(wQueueSize.getText(), 100));
		stepname = wStepname.getText(); // return value

		dispose();
	}
}
