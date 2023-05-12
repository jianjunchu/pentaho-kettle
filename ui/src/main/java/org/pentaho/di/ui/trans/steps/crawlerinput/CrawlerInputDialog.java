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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.TransMeta;
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
	private TextVar wContentPageURLPattern;
	private TextVar wStartPageURL;
	private TextVar wListPageURLPattern;
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
		wlStartPageURL.setText(BaseMessages.getString(PKG, "CrawlerInputDialog.StartPage.Label")); //$NON-NLS-1$
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

		// Content Page URL Pattern
		Label wlContentPageURLPattern = new Label(shell, SWT.RIGHT);
		wlContentPageURLPattern.setText(BaseMessages.getString(PKG, "CrawlerInputDialog.ContentPageURLPattern.Label")); //$NON-NLS-1$
		props.setLook(wlContentPageURLPattern);
		FormData fdlContentPageURLPattern = new FormData();
		fdlContentPageURLPattern.left = new FormAttachment(0, 0);
		fdlContentPageURLPattern.right= new FormAttachment(middle, -margin);
		fdlContentPageURLPattern.top  = new FormAttachment(wStartPageURL, margin);
		wlContentPageURLPattern.setLayoutData(fdlContentPageURLPattern);
		wContentPageURLPattern =new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wContentPageURLPattern.setText(stepname);
		props.setLook(wContentPageURLPattern);
		wContentPageURLPattern.addModifyListener(lsMod);
		FormData fdContentPageURLPattern = new FormData();
		fdContentPageURLPattern.left = new FormAttachment(middle, 0);
		fdContentPageURLPattern.top  = new FormAttachment(wStartPageURL, margin);
		fdContentPageURLPattern.right= new FormAttachment(100, 0);
		wContentPageURLPattern.setLayoutData(fdContentPageURLPattern);


		// ListPageURLPattern
		Label wlListPageURLPattern = new Label(shell, SWT.RIGHT);
		wlListPageURLPattern.setText(BaseMessages.getString(PKG, "CrawlerInputDialog.ListPageURLPattern.Label")); //$NON-NLS-1$
		props.setLook(wlListPageURLPattern);
		FormData fdlListPageURLPattern = new FormData();
		fdlListPageURLPattern.left = new FormAttachment(0, 0);
		fdlListPageURLPattern.right= new FormAttachment(middle, -margin);
		fdlListPageURLPattern.top  = new FormAttachment(wContentPageURLPattern, margin);
		wlListPageURLPattern.setLayoutData(fdlListPageURLPattern);
		wListPageURLPattern =new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wListPageURLPattern.setText(stepname);
		props.setLook(wListPageURLPattern);
		wListPageURLPattern.addModifyListener(lsMod);
		FormData fdListPageURLPattern = new FormData();
		fdListPageURLPattern.left = new FormAttachment(middle, 0);
		fdListPageURLPattern.top  = new FormAttachment(wContentPageURLPattern, margin);
		fdListPageURLPattern.right= new FormAttachment(100, 0);
		wListPageURLPattern.setLayoutData(fdListPageURLPattern);

		// row limit
		Label wlRowLimit = new Label(shell, SWT.RIGHT);
		props.setLook(wlRowLimit);
		wlRowLimit.setText(BaseMessages.getString(PKG, "CrawlerInputDialog.Rowlimit.Label"));
		FormData fdlRowLimit = new FormData();
		fdlRowLimit.top   = new FormAttachment(wListPageURLPattern, margin);
		fdlRowLimit.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlRowLimit.right = new FormAttachment(middle, 0);
		wlRowLimit.setLayoutData(fdlRowLimit);
		wRowLimit = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wRowLimit);
		FormData fdRowLimit = new FormData();
		fdRowLimit.top  = new FormAttachment(wListPageURLPattern, margin);
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
